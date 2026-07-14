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
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.settings.WidgetPreferences
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
        val prefs = PreferencesHelper.getWidgetPreferences(context)
        val weatherData: WeatherData?
        val birthdayData: BirthdayData?
        val status: WeatherUpdateStatus

        when (updateAction) {
            UpdateAction.REFRESH_THEN_FETCH -> {
                weatherData = DataRepository.loadWeatherData(context)
                birthdayData = DataRepository.loadBirthdayData(context)
                status = WidgetUpdater.refreshData(context, prefs, weatherData, birthdayData)
                fetchBirthdays(context, prefs)
                return handleWeather(context, prefs, status, birthdayData)
            }

            UpdateAction.BIRTHDAY_FETCH_THEN_REFRESH -> {
                weatherData = DataRepository.loadWeatherData(context)
                birthdayData = BirthdayService.fetchBirthdays(context)
                WidgetUpdater.refreshData(context, prefs, weatherData, birthdayData)
                return WorkResult.DATA_FETCH_DONE
            }

            UpdateAction.WEATHER_FETCH_THEN_REFRESH -> {
                weatherData = fetchWeather(context, prefs)
                birthdayData = DataRepository.loadBirthdayData(context)
                WidgetUpdater.refreshData(context, prefs, weatherData, birthdayData)
                return WorkResult.DATA_FETCH_DONE
            }
        }
    }

    suspend fun logUpdateStatus(context: Context, updateStatus: String) {
        val currentTimeMillis = System.currentTimeMillis()
        DataRepository.storeStatusData(context, StatusData(updateStatus, currentTimeMillis))
    }

    private suspend fun fetchBirthdays(context: Context, prefs: WidgetPreferences) {
        if (prefs.showBirthdays) BirthdayService.fetchBirthdays(context)
    }

    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    private suspend fun handleWeather(
        context: Context,
        prefs: WidgetPreferences,
        status: WeatherUpdateStatus,
        birthdayData: BirthdayData?
    ): WorkResult {
        if (!prefs.showWeather) return WorkResult.WIDGET_REFRESHED

        when (status) {
            WeatherUpdateStatus.DONE -> {
                return WorkResult.RECENT_DATA_SERVED
            }

            WeatherUpdateStatus.NEEDS_DATA -> {
                if (hasNetCapabilityInternet(context)) {
                    fetchWeather(context, prefs)
                    return WorkResult.FRESH_DATA_FETCHED
                } else return WorkResult.STALE_DATA_SERVED
            }

            WeatherUpdateStatus.NEEDS_DATA_AND_REFRESH -> {
                if (hasNetCapabilityInternet(context)) {
                    val weatherData = fetchWeather(context, prefs)
                    WidgetUpdater.refreshData(context, prefs, weatherData, birthdayData)
                    return WorkResult.FRESH_DATA_FETCHED
                } else return WorkResult.OUTDATED_DATA_SERVED
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    private suspend fun fetchWeather(
        context: Context,
        prefs: WidgetPreferences
    ): WeatherData {
        val locationData = LocationService.fetchLocation(context, prefs.useLocationFallback)
        return WeatherService.fetchWeather(context, locationData)
    }

    private fun hasNetCapabilityInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
