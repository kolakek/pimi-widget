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
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.TwoStatePreference
import androidx.work.ExistingWorkPolicy
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.data.KEY_WEATHER_SWITCH
import com.kolakek.pimiwidget.data.LocationSourcePref
import com.kolakek.pimiwidget.data.SettingsRepository
import com.kolakek.pimiwidget.location.FIXED_LOCATION_NAME
import com.kolakek.pimiwidget.location.LocationData
import com.kolakek.pimiwidget.worker.UpdateAction
import com.kolakek.pimiwidget.worker.WorkManagerHelper
import kotlinx.coroutines.launch

class WeatherSettingsHandler(private val fragment: PreferenceFragmentCompat) {

    private val permissionHandler = PermissionHandler(fragment)

    private val weatherSwitch get() =
        fragment.findPreference<TwoStatePreference>(KEY_WEATHER_SWITCH)

    fun refreshSwitch() {
        weatherSwitch?.isChecked = SettingsRepository.isWeatherEnabled(fragment.requireContext())
        enforcePermission()
    }

    fun weatherSwitchCallback(isChecked: Boolean): Boolean {
        if (!isChecked) {
            disableWeather()
            return true
        }
        if (usesDeviceLocation() && isCoarsePermissionDenied()) {
            disableWeather()
            permissionHandler.askPermission(
                ACCESS_COARSE_LOCATION,
                fragment.getString(R.string.config_loc_perm_alert_title),
                fragment.getString(R.string.config_loc_perm_alert_message)
            ) {
                weatherSwitchCallback(isChecked)
            }
            return false
        }
        if (usesDeviceLocation() && isBackgroundPermissionDenied()) {
            disableWeather()
            permissionHandler.askPermission(
                ACCESS_BACKGROUND_LOCATION,
                fragment.getString(R.string.config_bg_perm_alert_title),
                fragment.getString(
                    R.string.config_bg_perm_alert_message,
                    fragment.requireContext().packageManager.backgroundPermissionOptionLabel
                )
            ) {
                weatherSwitchCallback(isChecked)
            }
            return false
        }
        enableWeather()
        return true
    }

    fun selectWeatherLocation() {
        WeatherLocationDialog(
            context = fragment.requireContext(),
            scope = fragment.lifecycleScope,
            onDeviceLocationSelected = {
                deviceLocationCallback()
            },
            onFixedLocationSelected = { lat, long, place ->
                fixedLocationCallback(lat, long, place)
            },
        ).show()
    }

    fun usesDeviceLocation() =
        SettingsRepository.getLocationSourcePref(
            fragment.requireContext()
        ) == LocationSourcePref.DEVICE

    fun enforcePermission() {
        if (isWeatherEnabled() && usesDeviceLocation() && isBackgroundPermissionDenied()) {
            disableWeather()
        }
    }

    private fun isCoarsePermissionDenied(): Boolean =
        permissionHandler.isPermissionDenied(ACCESS_COARSE_LOCATION)

    private fun isBackgroundPermissionDenied(): Boolean =
        permissionHandler.isPermissionDenied(ACCESS_BACKGROUND_LOCATION)

    private fun isWeatherEnabled() =
        SettingsRepository.isWeatherEnabled(fragment.requireContext())

    private fun enableWeather() {
        weatherSwitch?.isChecked = true
        updateWeather()
    }

    private fun disableWeather() {
        weatherSwitch?.isChecked = false

        val context = fragment.requireContext()
        fragment.lifecycleScope.launch {
            DataRepository.deleteWeatherData(context)
            if (usesDeviceLocation()) DataRepository.deleteLocationData(context)
        }
    }

    private fun updateWeather() {
        WorkManagerHelper.enqueueOneTimeWork(
            fragment.requireContext(),
            action = UpdateAction.WEATHER_FETCH_THEN_REFRESH,
            workPolicy = ExistingWorkPolicy.REPLACE
        )
    }

    private fun deviceLocationCallback() {
        if (usesDeviceLocation()) return
        val context = fragment.requireContext()
        fragment.lifecycleScope.launch {
            DataRepository.deleteWeatherData(context)
            DataRepository.deleteLocationData(context)
            applyLocationSource(LocationSourcePref.DEVICE)
        }
    }

    private fun fixedLocationCallback(lat: Double, long: Double, place: String) {
        val context = fragment.requireContext()
        fragment.lifecycleScope.launch {
            DataRepository.deleteWeatherData(context)
            DataRepository.storeLocationData(
                context,
                LocationData(0, lat, long, place, FIXED_LOCATION_NAME)
            )
            applyLocationSource(LocationSourcePref.FIXED, place)
        }
    }

    private fun applyLocationSource(
        source: LocationSourcePref,
        summary: String = fragment.getString(source.labelRes),
    ) {
        SettingsRepository.setLocationSourcePref(fragment.requireContext(), source)
        fragment.findPreference<Preference>(KEY_FIELD_LOCATION_SOURCE)?.summary = summary
        if (isWeatherEnabled()) {
            enforcePermission()
            updateWeather()
        }
    }
}
