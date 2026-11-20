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

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import androidx.work.ExistingPeriodicWorkPolicy
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.data.KEY_DEFAULT_WEATHER_APP
import com.kolakek.pimiwidget.data.KEY_NIL_WEATHER_APP
import com.kolakek.pimiwidget.data.KEY_TEMP_UNIT_LIST
import com.kolakek.pimiwidget.data.KEY_WEATHER_APP_LIST
import com.kolakek.pimiwidget.data.KEY_WEATHER_SWITCH
import com.kolakek.pimiwidget.data.SettingsRepository
import com.kolakek.pimiwidget.data.TempUnitPref
import com.kolakek.pimiwidget.utility.AppLookup
import com.kolakek.pimiwidget.data.WeatherApp
import com.kolakek.pimiwidget.ui.LocationSearchActivity
import com.kolakek.pimiwidget.worker.WorkManagerHelper
import kotlinx.coroutines.launch

class WidgetSettingsWeatherFragment : CardPreferenceFragment() {
    override val titleRes = R.string.config_section_weather

    private val weatherHandler = WeatherSwitchHandler(this)

    private val locationField get() = findPreference<Preference>(KEY_FIELD_LOCATION_SOURCE)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pimi_widget_prefs_weather, rootKey)

        val context = requireContext()

        val weatherSwitch = findPreference<TwoStatePreference>(KEY_WEATHER_SWITCH)
        val tempUnitList = findPreference<ListPreference>(KEY_TEMP_UNIT_LIST)

        weatherSwitch?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) {
                WorkManagerHelper.enqueuePeriodicWork(
                    context,
                    workPolicy = ExistingPeriodicWorkPolicy.KEEP
                )
            }
            weatherHandler.weatherSwitchCallback(newValue == true)
        }
        tempUnitList?.setOptions(
            options = TempUnitPref.entries,
            default = TempUnitPref.DEFAULT,
            keyOf = { it.key },
            labelOf = { it.labelRes }
        )
        locationField?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), LocationSearchActivity::class.java))
            true
        }
        setWeatherLocationSummary()
        handleWeatherAppPreference()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weatherHandler.refreshSwitch()
    }

    override fun onResume() {
        super.onResume()
        setWeatherLocationSummary()
        weatherHandler.refreshSwitch()
    }

    private fun setWeatherLocationSummary() {
        val context = requireContext()
        lifecycleScope.launch {
            locationField?.summary =
                if (SettingsRepository.usesDeviceLocation(context)) {
                    getString(R.string.config_weather_location_device)
                } else {
                    DataRepository.loadLocationData(context)?.place
                }
        }
    }

    private fun handleWeatherAppPreference() {
        val installedApps = WeatherApp.entries.filter { app ->
            app == WeatherApp.PIMI || AppLookup.isAppInstalled(requireContext(), app.packageName)
        }
        val listPreference = findPreference<ListPreference>(KEY_WEATHER_APP_LIST)
        val entries = listPreference?.entries?.toMutableList() ?: mutableListOf()
        val entryValues = listPreference?.entryValues?.toMutableList() ?: mutableListOf()

        installedApps.forEach {
            entries.add(getString(it.labelRes))
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
        listPreference?.summaryProvider = Preference.SummaryProvider<Preference> {
            getString(
                R.string.config_weather_app_summary,
                getString(SettingsRepository.getWeatherApp(requireContext()).labelRes)
            )
        }
    }

    private fun showUnlistedAppDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.config_weather_app_nil_alert_message)
            .setPositiveButton(R.string.config_weather_app_alert_button_github) { dialog, _ ->
                openUrl(ISSUE_TRACKER_URL)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.config_alert_button_cancel, null)
            .show()
    }
}
