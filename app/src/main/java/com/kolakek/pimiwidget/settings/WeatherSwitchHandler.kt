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
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.TwoStatePreference
import androidx.work.ExistingWorkPolicy
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.data.KEY_WEATHER_SWITCH
import com.kolakek.pimiwidget.data.SettingsRepository
import com.kolakek.pimiwidget.location.LocationService
import com.kolakek.pimiwidget.worker.UpdateAction
import com.kolakek.pimiwidget.worker.WorkManagerHelper
import kotlinx.coroutines.launch

class WeatherSwitchHandler(private val fragment: PreferenceFragmentCompat) {

    private val permissionHandler = PermissionHandler(fragment)

    private val context get() = fragment.requireContext()

    private val weatherSwitch get() =
        fragment.findPreference<TwoStatePreference>(KEY_WEATHER_SWITCH)

    fun refreshSwitch() {
        val weatherEnabled = SettingsRepository.isWeatherEnabled(context)

        if (weatherEnabled && LocationService.isPermissionMissing(context)) {
            disableWeather()
        } else {
            weatherSwitch?.isChecked = weatherEnabled
        }
    }

    fun weatherSwitchCallback(isChecked: Boolean): Boolean {
        if (!isChecked) {
            disableWeather()
            return true
        }
        val usesDeviceLocation = SettingsRepository.usesDeviceLocation(context)

        val locationGranted = permissionHandler.isPermissionGranted(ACCESS_COARSE_LOCATION)
        val backgroundGranted = permissionHandler.isPermissionGranted(ACCESS_BACKGROUND_LOCATION)

        if (usesDeviceLocation && !locationGranted) {
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
        if (usesDeviceLocation && !backgroundGranted) {
            disableWeather()
            permissionHandler.askPermission(
                ACCESS_BACKGROUND_LOCATION,
                fragment.getString(R.string.config_bg_perm_alert_title),
                fragment.getString(
                    R.string.config_bg_perm_alert_message,
                    context.packageManager.backgroundPermissionOptionLabel
                )
            ) {
                weatherSwitchCallback(isChecked)
            }
            return false
        }
        enableWeather()
        return true
    }

    private fun enableWeather() {
        weatherSwitch?.isChecked = true
        WorkManagerHelper.enqueueOneTimeWork(
            context,
            UpdateAction.WEATHER_FETCH_THEN_REFRESH,
            ExistingWorkPolicy.REPLACE
        )
    }

    private fun disableWeather() {
        weatherSwitch?.isChecked = false
        val context = context
        fragment.lifecycleScope.launch {
            DataRepository.deleteWeatherData(context)
            if (SettingsRepository.usesDeviceLocation(context))
                DataRepository.deleteLocationData(context)
        }
    }
}
