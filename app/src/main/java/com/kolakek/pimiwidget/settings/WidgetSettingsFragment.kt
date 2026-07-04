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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.kolakek.pimiwidget.BuildConfig
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.location.LocationData
import com.kolakek.pimiwidget.utility.AppLookup
import com.kolakek.pimiwidget.utility.WeatherApp
import com.kolakek.pimiwidget.weather.WeatherService
import com.kolakek.pimiwidget.worker.WorkManagerHelper
import io.ktor.http.URLBuilder
import kotlinx.coroutines.launch
import java.util.Date

internal class WidgetSettingsFragment : PreferenceFragmentCompat() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) weatherSwitchCallback(preferenceManager.context, true) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pimi_widget_prefs, rootKey)

        val context = preferenceManager.context
        val weatherSwitch: SwitchPreferenceCompat? = findPreference(KEY_WEATHER_SWITCH)
        val versionField: LongPressPreference? = findPreference(KEY_VERSION_FIELD)
        val sourceCodeField: Preference? = findPreference(KEY_SOURCE_CODE)
        val settingsPlus: Preference? = findPreference(KEY_SETTINGS_PLUS)

        var debugCount = 0

        if (weatherSwitch?.isChecked == true &&
            hasNoPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            WorkManagerHelper.cancelWork(context)
            deleteAllData(context)
            weatherSwitch.isChecked = false
        }
        versionField?.setOnPreferenceClickListener {
            if (debugCount == 2) {
                settingsPlus?.isVisible = true
            }
            else
                debugCount++
            true
        }
        versionField?.setOnLongClickListener {
            showDebugDialog(context)
        }
        versionField?.summary = BuildConfig.VERSION_CODE.toString()

        sourceCodeField?.setOnPreferenceClickListener {
            startUrlActivity(SOURCE_CODE_URL)
            true
        }
        weatherSwitch?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true)
                return@setOnPreferenceChangeListener weatherSwitchCallback(context, true)

            if (newValue == false)
                return@setOnPreferenceChangeListener weatherSwitchCallback(context, false)

            false
        }
        handleWeatherAppPreference(context)
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

    private fun weatherSwitchCallback(context: Context, flag: Boolean): Boolean {
        val weatherSwitch: SwitchPreferenceCompat? = findPreference(KEY_WEATHER_SWITCH)

        if (!flag) {
            WorkManagerHelper.cancelWork(context)
            deleteAllData(context)
            weatherSwitch?.isChecked = false

            return true
        }
        if (hasNoPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            askForPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                getString(R.string.config_loc_perm_alert_title),
                getString(R.string.config_loc_perm_alert_message)
            )
            return false
        }
        if (hasNoPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            askForPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                getString(R.string.config_bg_perm_alert_title),
                getString(
                    R.string.config_bg_perm_alert_message,
                    context.packageManager.backgroundPermissionOptionLabel
                )
            )
            return false
        }
        WorkManagerHelper.enqueueWork(context)
        weatherSwitch?.isChecked = true

        return true
    }

    private fun askForPermission(
        context: Context,
        permission: String,
        title: String,
        message: String
    ) {
        if (shouldShowRequestPermissionRationale(permission)) {
            showRationaleDialog(context, permission, title, message)
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun hasNoPermission(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_DENIED

    private fun showDebugDialog(
        context: Context
    ) {
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
        context: Context,
        permission: String,
        title: String,
        message: String
    ) {
        AlertDialog.Builder(context)
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

    private fun deleteAllData(context: Context) {
        lifecycleScope.launch {
            DataRepository.deleteAllData(context)
        }
    }

    private fun handleWeatherAppPreference(context: Context) {
        val installedApps = WeatherApp.entries.filter { app ->
            AppLookup.isAppInstalled(context, app.packageName)
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
                showUnlistedAppDialog(context)
                return@setOnPreferenceChangeListener false
            }
            true
        }
    }

    private fun showUnlistedAppDialog(
        context: Context,
    ) {
        AlertDialog.Builder(context)
            .setMessage(R.string.config_weather_app_nil_alert_message)
            .setPositiveButton(R.string.config_weather_app_alert_button_github) { dialog, _ ->
                startUrlActivity(ISSUE_TRACKER_URL)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.config_alert_button_cancel, null)
            .show()
    }
}
