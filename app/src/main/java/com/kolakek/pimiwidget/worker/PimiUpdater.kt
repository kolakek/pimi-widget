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

package com.kolakek.pimiwidget.worker

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import com.kolakek.pimiwidget.birthday.BirthdayData
import com.kolakek.pimiwidget.birthday.BirthdayService
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.location.LocationService
import com.kolakek.pimiwidget.data.SettingsRepository
import com.kolakek.pimiwidget.widget.WidgetSettings
import com.kolakek.pimiwidget.weather.WeatherData
import com.kolakek.pimiwidget.weather.WeatherService
import com.kolakek.pimiwidget.widget.WeatherUpdateStatus
import com.kolakek.pimiwidget.widget.WidgetUpdater

object PimiUpdater {

    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    suspend fun update(
        context: Context,
        updateAction: UpdateAction,
    ): WorkResult {
        val settings = SettingsRepository.loadWidgetSettings(context)
        val weatherData: WeatherData?
        val birthdayData: BirthdayData?
        val status: WeatherUpdateStatus

        when (updateAction) {
            UpdateAction.REFRESH_THEN_FETCH -> {
                weatherData = DataRepository.loadWeatherData(context)
                birthdayData = DataRepository.loadBirthdayData(context)
                status = WidgetUpdater.refreshWidgets(context, settings, weatherData, birthdayData)
                fetchBirthdays(context, settings)
                return handleWeather(context, settings, status)
            }

            UpdateAction.BIRTHDAY_FETCH_THEN_REFRESH -> {
                birthdayData = BirthdayService.fetchBirthdays(context)
                WidgetUpdater.refreshBirthdays(context, settings, birthdayData)
                return WorkResult.WORK_COMPLETE
            }

            UpdateAction.WEATHER_FETCH_THEN_REFRESH -> {
                return handleWeather(context, settings, WeatherUpdateStatus.NEEDS_DATA_AND_REFRESH)
            }
        }
    }

    suspend fun logUpdateStatus(context: Context, updateStatus: String) {
        val currentTimeMillis = System.currentTimeMillis()
        DataRepository.storeStatusData(context, StatusData(updateStatus, currentTimeMillis))
    }

    private suspend fun fetchBirthdays(context: Context, settings: WidgetSettings) {
        if (settings.showBirthday) BirthdayService.fetchBirthdays(context)
    }

    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    private suspend fun handleWeather(
        context: Context,
        settings: WidgetSettings,
        status: WeatherUpdateStatus
    ): WorkResult {
        if (!settings.showWeather) return WorkResult.WORK_COMPLETE

        when (status) {
            WeatherUpdateStatus.NO_ACTION_NEEDED -> {
                return WorkResult.WORK_COMPLETE
            }

            WeatherUpdateStatus.NEEDS_DATA -> {
                if (hasNetCapabilityInternet(context)) {
                    fetchWeather(context, settings)
                    return WorkResult.DATA_FETCH_COMPLETE
                } else return WorkResult.NETWORK_UNAVAILABLE
            }

            WeatherUpdateStatus.NEEDS_DATA_AND_REFRESH -> {
                if (hasNetCapabilityInternet(context)) {
                    val weatherData = fetchWeather(context, settings)
                    WidgetUpdater.refreshWeather(context, settings, weatherData)
                    return WorkResult.DATA_UPDATE_COMPLETE
                } else return WorkResult.NETWORK_UNAVAILABLE
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    private suspend fun fetchWeather(
        context: Context,
        settings: WidgetSettings
    ): WeatherData {
        val locationData = LocationService.fetchLocation(
            context,
            settings.locationSource,
            settings.useLocationFallback
        )
        return WeatherService.fetchWeather(context, locationData)
    }

    private fun hasNetCapabilityInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
