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
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.kolakek.pimiwidget.BuildConfig
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.DataKeys
import com.kolakek.pimiwidget.data.JsonDataStore
import com.kolakek.pimiwidget.location.LocationService
import com.kolakek.pimiwidget.weather.WeatherService
import com.kolakek.pimiwidget.worker.StatusData
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
        val debugField: Preference? = findPreference(KEY_DEBUG_INFO)
        val sharedDataField: Preference? = findPreference(KEY_SHARED_DATA)
        val sourceCodeField: Preference? = findPreference(KEY_SOURCE_CODE)

        var debugCount = 0

        if (weatherSwitch?.isChecked == true &&
            hasNoPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            weatherSwitch.isChecked = false
            WorkManagerHelper.cancelWorkers(context)
        }
        debugField?.setOnPreferenceClickListener {
            if (debugCount == 2)
                showDebugDialog(context)
            else
                debugCount++
            true
        }
        debugField?.summary = BuildConfig.VERSION_NAME

        sharedDataField?.setOnPreferenceClickListener {
            showDataInfoDialog(context)
            true
        }
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
            WorkManagerHelper.cancelWorkers(context)
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
        WorkManagerHelper.enqueuePeriodicWorker(context)
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
            .setMessage(getString(R.string.config_debug_alert_message, "-", "-", "-"))
            .setCancelable(true)
            .setPositiveButton(R.string.config_alert_button_ok, null)
            .show()

        lifecycleScope.launch {
            val weatherData = WeatherService.getWeatherData(context, DataKeys.WEATHER_DATA_KEY)
            val dataAgeStr = weatherData?.timeMillis?.let { createAgeString(it) } ?: "-"

            val statusData: StatusData? = JsonDataStore.load(
                context, DataKeys.STATUS_DATA_KEY
            )
            val statusStr = statusData?.let {
                getString(
                    R.string.config_debug_alert_status_time,
                    it.status,
                    DateFormat.getTimeFormat(context).format(Date(it.timeMillis))
                )
            } ?: "-"

            val workerStatusStr = WorkManagerHelper.getWorkerStatus(context) ?: "-"

            val workerStr = WorkManagerHelper.getNextScheduleMillis(context)?.let {
                getString(
                    R.string.config_debug_alert_status_time,
                    workerStatusStr,
                    DateFormat.getTimeFormat(context).format(Date(it))
                )
            } ?: workerStatusStr

            dialog.setMessage(
                getString(R.string.config_debug_alert_message, dataAgeStr, statusStr, workerStr)
            )
        }
    }

    private fun showDataInfoDialog(context: Context) {
        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.config_shared_data)
            .setMessage(R.string.config_shared_data_alert_message)
            .setCancelable(true)
            .setNegativeButton(R.string.config_shared_data_alert_but_location, null)
            .setPositiveButton(R.string.config_shared_data_alert_but_weather, null)
            .setNeutralButton(R.string.config_alert_button_cancel, null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            viewLocationCallback(context, dialog)
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            viewWeatherCallback(context, dialog)
        }
    }

    private fun viewLocationCallback(context: Context, dialog: AlertDialog) {
        lifecycleScope.launch {
            dialog.setMessage(getString(R.string.config_shared_data_alert_loading))
            LocationService.getLocationData(context, DataKeys.LOCATION_DATA_KEY)?.let {
                startUrlActivity(
                    URLBuilder(LOCATION_URL).apply {
                        parameters.append("mlat", it.lat.toString())
                        parameters.append("mlon", it.long.toString())
                        fragment = "map=$LOCATION_URL_ZOOM/${it.lat}/${it.long}"
                    }.toString()
                )
                dialog.dismiss()
            } ?: dialog.setMessage(getString(R.string.config_shared_data_alert_no_data))
        }
    }

    private fun viewWeatherCallback(context: Context, dialog: AlertDialog) {
        lifecycleScope.launch {
            dialog.setMessage(getString(R.string.config_shared_data_alert_loading))
            LocationService.getLocationData(context, DataKeys.LOCATION_DATA_KEY)?.let {
                startUrlActivity(WeatherService.weatherUrl(it, "iso8601").toString())
                dialog.dismiss()
            } ?: dialog.setMessage(getString(R.string.config_shared_data_alert_no_data))
        }
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

    private fun createAgeString(timeMillis: Long): String {
        val ageMins: Int = ((System.currentTimeMillis() - timeMillis) / 1000L / 60L).toInt()
        val ageHours = ageMins / 60
        val ageDays = ageHours / 24
        return when {
            ageMins < 120 -> resources.getQuantityString(R.plurals.minutes, ageMins, ageMins)
            ageMins < 60 * 48 -> resources.getQuantityString(R.plurals.hours, ageHours, ageHours)
            else -> resources.getQuantityString(R.plurals.days, ageDays, ageDays)
        }
    }
}
