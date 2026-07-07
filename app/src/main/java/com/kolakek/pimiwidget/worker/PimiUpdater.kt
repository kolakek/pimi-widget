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
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.location.LocationService
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.weather.WeatherService
import com.kolakek.pimiwidget.widget.WeatherUpdateStatus
import com.kolakek.pimiwidget.widget.WidgetUpdater

object PimiUpdater {

    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    suspend fun update(
        context: Context,
    ): WorkResult {
        val prefs = PreferencesHelper.getWidgetPreferences(context)
        val weatherData = DataRepository.loadWeatherData(context)
        val status = WidgetUpdater.refreshWidgetData(context, prefs, weatherData)

        if (!prefs.showWeather) return WorkResult.WIDGET_REFRESHED

        when (status.weatherUpdate) {
            WeatherUpdateStatus.DONE ->
                return WorkResult.RECENT_DATA_SERVED

            WeatherUpdateStatus.NEEDS_DATA -> {
                fetchWeatherData(context, prefs)?.let {
                    return WorkResult.FRESH_DATA_FETCHED
                }
                return WorkResult.STALE_DATA_SERVED
            }
            WeatherUpdateStatus.NEEDS_DATA_AND_REFRESH -> {
                fetchWeatherData(context, prefs)?.let {
                    WidgetUpdater.refreshWidgetData(context, prefs, it)
                    return WorkResult.FRESH_DATA_FETCHED
                }
                return WorkResult.INVALID_DATA_SERVED
            }
        }
    }

    suspend fun logUpdateStatus(
        context: Context,
        updateStatus: String,
    ) {
        DataRepository.storeStatusData(
            context,
            StatusData(updateStatus, System.currentTimeMillis())
        )
    }

    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    private suspend fun fetchWeatherData(context: Context, prefs: WidgetPreferences) =
        if (hasNetCapabilityInternet(context)) {
            WeatherService.fetchWeatherData(
                context,
                LocationService.fetchLocation(
                    context,
                    prefs.useLocationFallback
                )
            )
        } else null

    private fun hasNetCapabilityInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
