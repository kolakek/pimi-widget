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
import androidx.work.ExistingPeriodicWorkPolicy
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.location.LocationService
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.weather.WeatherService
import com.kolakek.pimiwidget.widget.WidgetUpdateStatus
import com.kolakek.pimiwidget.widget.WidgetUpdater

internal object PimiUpdater {

    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    internal suspend fun update(
        context: Context,
        isRecoveryMode: Boolean = false,
        runAttemptCount: Int
    ): WorkResult {
        val prefs = PreferencesHelper.getWidgetPreferences(context)

        if (isRecoveryMode) {

            if (!hasNetCapabilityValidated(context) && runAttemptCount < MAX_NUM_RETRIES) {
                return WorkResult.INTERNET_FAILED
            }
            val location = LocationService.fetchLocation(context, prefs.useLocationFallback)
            val weatherData = WeatherService.fetchWeatherData(context, location)

            WidgetUpdater.partiallyUpdateWidgets(context, prefs, weatherData)

            WorkManagerHelper.enqueueWork(context, ExistingPeriodicWorkPolicy.UPDATE, false)

            return WorkResult.FRESH_DATA_FETCHED
        }
        val weatherData = DataRepository.loadWeatherData(context)
        val status = WidgetUpdater.partiallyUpdateWidgets(context, prefs, weatherData)

        val dataTimeMillis = weatherData?.timeMillis ?: 0
        val dataAgeMillis = System.currentTimeMillis() - dataTimeMillis

        val isDataFresh = dataAgeMillis < DATA_MAX_AGE_MILLIS
        val isDataValid = status == WidgetUpdateStatus.SUCCESS

        if (isDataValid && isDataFresh) return WorkResult.RECENT_DATA_SERVED

        if (hasNetCapabilityInternet(context)) {
            val location = LocationService.fetchLocation(context, prefs.useLocationFallback)
            val freshWeatherData = WeatherService.fetchWeatherData(context, location)

            if (!isDataValid) WidgetUpdater.partiallyUpdateWidgets(context, prefs, freshWeatherData)

            return WorkResult.FRESH_DATA_FETCHED

        } else if (!isDataValid) {
            WorkManagerHelper.enqueueWork(context, ExistingPeriodicWorkPolicy.UPDATE, true)

            return WorkResult.RECOVERY_ENQUEUED
        }
        return WorkResult.STALE_DATA_SERVED
    }

    internal suspend fun logUpdateStatus(
        context: Context,
        updateStatus: String,
    ) {
        DataRepository.storeStatusData(
            context,
            StatusData(updateStatus, System.currentTimeMillis())
        )
    }

    private fun hasNetCapabilityInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun hasNetCapabilityValidated(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
