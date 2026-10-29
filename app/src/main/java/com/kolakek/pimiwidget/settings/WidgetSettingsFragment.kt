/*
 * This file is part of Pimi Widget.
 *
 * Pimi Widget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.kolakek.pimiwidget.settings

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.READ_CONTACTS
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import com.kolakek.pimiwidget.BuildConfig
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.location.FIXED_LOCATION_NAME
import com.kolakek.pimiwidget.location.LocationData
import com.kolakek.pimiwidget.utility.AppLookup
import com.kolakek.pimiwidget.utility.WeatherApp
import com.kolakek.pimiwidget.weather.WeatherService
import com.kolakek.pimiwidget.widget.BirthdayUpdater
import com.kolakek.pimiwidget.worker.UpdateAction
import com.kolakek.pimiwidget.worker.WorkManagerHelper
import io.ktor.http.URLBuilder
import kotlinx.coroutines.launch
import java.util.Date

class WidgetSettingsFragment : PreferenceFragmentCompat() {

    private var onPermissionGranted: (() -> Unit)? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { onPermissionGranted?.invoke() }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pimi_widget_prefs, rootKey)

        val context = requireContext()
        val weatherSwitch: SwitchPreferenceCompat? = findPreference(KEY_WEATHER_SWITCH)
        val birthdaySwitch: SwitchPreferenceCompat? = findPreference(KEY_BIRTHDAY_SWITCH)
        val alarmSwitch: SwitchPreferenceCompat? = findPreference(KEY_ALARM_SWITCH)
        val versionField: LongPressPreference? = findPreference(KEY_VERSION_FIELD)
        val sourceCodeField: Preference? = findPreference(KEY_SOURCE_CODE)
        val widgetStyleList: ListPreference? = findPreference(KEY_WIDGET_STYLE_LIST)
        val weatherLocationField: Preference? = findPreference(KEY_WEATHER_LOCATION)

        var debugCount = 0

        if (weatherSwitch?.isChecked == true &&
            usesDeviceLocation() &&
            isPermissionDenied(ACCESS_BACKGROUND_LOCATION)
        ) {
            disableWeather()
        }
        if (birthdaySwitch?.isChecked == true &&
            isPermissionDenied(READ_CONTACTS)
        ) {
            deleteBirthdayData()
            birthdaySwitch.isChecked = false
        }
        versionField?.setOnPreferenceClickListener {
            if (debugCount == 2) {
                parentFragmentManager
                    .beginTransaction()
                    .setTransition(TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.widget_settings_fragment, WidgetSettingsPlusFragment())
                    .addToBackStack(null)
                    .commit()
            } else
                debugCount++
            true
        }
        versionField?.setOnLongClickListener {
            showDebugDialog()
        }
        versionField?.summary = BuildConfig.VERSION_CODE.toString()

        sourceCodeField?.setOnPreferenceClickListener {
            startUrlActivity(SOURCE_CODE_URL)
            true
        }
        weatherSwitch?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) {
                WorkManagerHelper.enqueuePeriodicWork(
                    context,
                    workPolicy = ExistingPeriodicWorkPolicy.KEEP
                )
            }
            weatherSwitchCallback(newValue == true)
        }
        birthdaySwitch?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) {
                WorkManagerHelper.enqueuePeriodicWork(
                    context,
                    workPolicy = ExistingPeriodicWorkPolicy.KEEP
                )
            }
            birthdaySwitchCallback(newValue == true)
        }
        alarmSwitch?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) {
                WorkManagerHelper.enqueuePeriodicWork(
                    context,
                    workPolicy = ExistingPeriodicWorkPolicy.KEEP
                )
            }
            true
        }
        widgetStyleList?.setOnPreferenceChangeListener { _, newValue ->
            PreferencesHelper.setTextColorPreference(context, PreferencesHelper.ColorPref.AUTO)
            PreferencesHelper.setIconColorPreference(context, PreferencesHelper.ColorPref.AUTO)
            PreferencesHelper.setAuxDisplayPreference(
                context,
                if (newValue == KEY_WIDGET_STYLE_SOLID) {
                    PreferencesHelper.AuxDisplayPref.PLACE_CONDITION
                }
                else PreferencesHelper.AuxDisplayPref.NOTHING
            )
            true
        }
        weatherLocationField?.setOnPreferenceClickListener {
            selectWeatherLocation()
            true
        }
        setWeatherLocationSummary()
        handleWeatherAppPreference()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView.clipToPadding = false

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            listView.updatePadding(bottom = bottomInset)
            insets
        }
    }

    private fun weatherSwitchCallback(isChecked: Boolean): Boolean {
        if (!isChecked) {
            disableWeather()
            return true
        }
        if (usesDeviceLocation() && isPermissionDenied(ACCESS_COARSE_LOCATION)) {
            disableWeather()

            askPermission(
                ACCESS_COARSE_LOCATION,
                getString(R.string.config_loc_perm_alert_title),
                getString(R.string.config_loc_perm_alert_message)
            ) {
                weatherSwitchCallback(true)
            }
            return false
        }
        if (usesDeviceLocation() && isPermissionDenied(ACCESS_BACKGROUND_LOCATION)) {
            disableWeather()

            askPermission(
                ACCESS_BACKGROUND_LOCATION,
                getString(R.string.config_bg_perm_alert_title),
                getString(
                    R.string.config_bg_perm_alert_message,
                    requireContext().packageManager.backgroundPermissionOptionLabel
                )
            ) {
                weatherSwitchCallback(true)
            }
            return false
        }
        enableWeather()

        return true
    }

    private fun deviceLocationCallback() {
        lifecycleScope.launch {
            DataRepository.deleteLocationData(requireContext())

            preferenceManager.sharedPreferences?.edit {
                putString(KEY_WEATHER_LOCATION, KEY_WEATHER_LOCATION_DEVICE)
            }
            findPreference<Preference>(KEY_WEATHER_LOCATION)?.summary =
                getString(R.string.config_weather_location_device)

            weatherSwitchCallback(isWeatherEnabled())
        }
    }

    private fun fixedLocationCallback(lat: Double, long: Double, place: String) {
        lifecycleScope.launch {
            DataRepository.storeLocationData(
                requireContext(),
                LocationData(0, lat, long, place, FIXED_LOCATION_NAME)
            )
            preferenceManager.sharedPreferences?.edit {
                putString(KEY_WEATHER_LOCATION, KEY_WEATHER_LOCATION_FIXED)
            }
            findPreference<Preference>(KEY_WEATHER_LOCATION)?.summary = place

            weatherSwitchCallback(isWeatherEnabled())
        }
    }

    private fun selectWeatherLocation() {
        WeatherLocationDialog(
            context = requireContext(),
            scope = lifecycleScope,
            onDeviceLocationSelected = {
                deviceLocationCallback()
            },
            onFixedLocationSelected = { lat, long, place ->
                fixedLocationCallback(lat, long, place)
            },
        ).show()
    }

    private fun setWeatherLocationSummary() {
        lifecycleScope.launch {
            findPreference<Preference>(KEY_WEATHER_LOCATION)?.summary =
                if (usesDeviceLocation()) {
                    getString(R.string.config_weather_location_device)
                } else {
                    DataRepository.loadLocationData(requireContext())?.place
                }
        }
    }

    private fun birthdaySwitchCallback(isChecked: Boolean): Boolean {
        val birthdaySwitch: SwitchPreferenceCompat? = findPreference(KEY_BIRTHDAY_SWITCH)

        if (!isChecked) {
            deleteBirthdayData()
            birthdaySwitch?.isChecked = false

            return true
        }
        if (isPermissionDenied(READ_CONTACTS)) {
            askPermission(
                READ_CONTACTS,
                getString(R.string.config_contact_perm_alert_title),
                getString(R.string.config_contact_perm_alert_message),
            ) {
                birthdaySwitchCallback(true)
            }
            return false
        }
        WorkManagerHelper.enqueueOneTimeWork(
            requireContext(),
            action = UpdateAction.BIRTHDAY_FETCH_THEN_REFRESH,
            workPolicy = ExistingWorkPolicy.REPLACE
        )
        birthdaySwitch?.isChecked = true

        return true
    }

    private fun askPermission(
        permission: String,
        title: String,
        message: String,
        onGranted: () -> Unit
    ) {
        onPermissionGranted = { if (isPermissionGranted(permission)) onGranted() }
        if (shouldShowRequestPermissionRationale(permission)) {
            showRationaleDialog(permission, title, message)
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(requireContext(), permission) ==
                PackageManager.PERMISSION_GRANTED

    private fun isPermissionDenied(permission: String): Boolean =
        !isPermissionGranted(permission)

    private fun usesDeviceLocation() =
        preferenceManager.sharedPreferences?.getString(
            KEY_WEATHER_LOCATION, KEY_WEATHER_LOCATION_DEVICE
        ) == KEY_WEATHER_LOCATION_DEVICE

    private fun isWeatherEnabled() =
        findPreference<SwitchPreferenceCompat>(KEY_WEATHER_SWITCH)?.isChecked == true

    private fun enableWeather() {
        WorkManagerHelper.enqueueOneTimeWork(
            requireContext(),
            action = UpdateAction.WEATHER_FETCH_THEN_REFRESH,
            workPolicy = ExistingWorkPolicy.REPLACE
        )
        findPreference<SwitchPreferenceCompat>(KEY_WEATHER_SWITCH)?.isChecked = true
    }

    private fun disableWeather() {
        deleteWeatherData()
        findPreference<SwitchPreferenceCompat>(KEY_WEATHER_SWITCH)?.isChecked = false
    }

    private fun showDebugDialog() {
        val context = requireContext()

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.config_debug_info)
            .setMessage(createDebugMessage("-", "-", "-", "-"))
            .setCancelable(true)
            .setPositiveButton(R.string.config_debug_button_location, null)
            .setNegativeButton(R.string.config_debug_button_weather, null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { }

        lifecycleScope.launch {
            val weatherData = DataRepository.loadWeatherData(context)
            val dataAgeStr = weatherData?.timeMillis?.let {
                createAgeString(it)
            } ?: "-"

            val locationData = DataRepository.loadLocationData(context)
            val locationStr = locationData?.locationType ?: "-"

            val statusData = DataRepository.loadStatusData(context)
            val statusStr = statusData?.let {
                getString(
                    R.string.config_debug_alert_status_time,
                    it.status,
                    DateFormat.getTimeFormat(context).format(Date(it.timeMillis))
                )
            } ?: "-"

            val workStatusStr = WorkManagerHelper.getStatus(context) ?: "-"
            val workStr = WorkManagerHelper.getNextRunMillis(context)?.let {
                getString(
                    R.string.config_debug_alert_status_time,
                    workStatusStr,
                    DateFormat.getTimeFormat(context).format(Date(it))
                )
            } ?: workStatusStr

            dialog.setMessage(
                createDebugMessage(workStr, statusStr, locationStr, dataAgeStr)
            )
            locationData?.let {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    viewLocationCallback(dialog, locationData)
                }
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                    viewWeatherCallback(dialog, locationData)
                }
            }
        }
    }

    private fun viewLocationCallback(
        dialog: AlertDialog,
        locationData: LocationData
    ) {
        startUrlActivity(
            URLBuilder(LOCATION_URL).apply {
                parameters.append("mlat", locationData.lat.toString())
                parameters.append("mlon", locationData.long.toString())
                fragment = "map=$LOCATION_URL_ZOOM/${locationData.lat}/${locationData.long}"
            }.toString()
        )
        dialog.dismiss()
    }

    private fun viewWeatherCallback(
        dialog: AlertDialog,
        locationData: LocationData
    ) {
        startUrlActivity(WeatherService.weatherUrl(locationData, "iso8601").toString())
        dialog.dismiss()
    }

    private fun startUrlActivity(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    private fun showRationaleDialog(
        permission: String,
        title: String,
        message: String
    ) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setTitle(title)
            .setCancelable(false)
            .setPositiveButton(R.string.config_alert_button_ok) { dialog, _ ->
                requestPermissionLauncher.launch(permission)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.config_alert_button_cancel, null)
            .show()
    }

    private fun createDebugMessage(
        s1: String,
        s2: String,
        s3: String,
        s4: String
    ): String = "\n${getString(R.string.config_debug_background_service)}\n$s1\n\n" +
            "${getString(R.string.config_debug_last_work_status)}\n$s2\n\n" +
            "${getString(R.string.config_debug_last_valid_location)}\n$s3\n\n" +
            "${getString(R.string.config_debug_weather_data_age)}\n$s4"

    private fun createAgeString(timeMillis: Long): String {
        val ageMins: Int = ((System.currentTimeMillis() - timeMillis) / 1000L / 60L).toInt()
        val ageHours = ageMins / 60
        val ageDays = ageHours / 24
        return when {
            ageMins < 180 -> resources.getQuantityString(R.plurals.minutes, ageMins, ageMins)
            ageMins < 60 * 48 -> resources.getQuantityString(R.plurals.hours, ageHours, ageHours)
            else -> resources.getQuantityString(R.plurals.days, ageDays, ageDays)
        }
    }

    private fun deleteWeatherData() {
        lifecycleScope.launch {
            DataRepository.deleteWeatherData(requireContext())
        }
    }

    private fun deleteBirthdayData() {
        lifecycleScope.launch {
            BirthdayUpdater.deleteDismissHash(requireContext())
            DataRepository.deleteBirthdayData(requireContext())
        }
    }

    private fun handleWeatherAppPreference() {
        val installedApps = WeatherApp.entries.filter { app ->
            AppLookup.isAppInstalled(requireContext(), app.packageName)
        }
        val listPreference = findPreference<ListPreference>(KEY_WEATHER_APP_LIST)
        val entries = listPreference?.entries?.toMutableList() ?: mutableListOf()
        val entryValues = listPreference?.entryValues?.toMutableList() ?: mutableListOf()

        installedApps.forEach {
            entries.add(getString(it.labelId))
            entryValues.add(it.key)
        }
        entries.add(getString(R.string.config_weather_app_nil))
        entryValues.add(KEY_NIL_WEATHER_APP)

        listPreference?.entries = entries.toTypedArray()
        listPreference?.entryValues = entryValues.toTypedArray()

        val currentApp = listPreference?.value
        val isCurrentAppInstalled = installedApps.any {
            it.key == currentApp
        }
        if (!isCurrentAppInstalled) {
            listPreference?.value = KEY_DEFAULT_WEATHER_APP
        }
        listPreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == KEY_NIL_WEATHER_APP) {
                showUnlistedAppDialog()
                return@setOnPreferenceChangeListener false
            }
            true
        }
    }

    private fun showUnlistedAppDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.config_weather_app_nil_alert_message)
            .setPositiveButton(R.string.config_weather_app_alert_button_github) { dialog, _ ->
                startUrlActivity(ISSUE_TRACKER_URL)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.config_alert_button_cancel, null)
            .show()
    }
}
