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
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.kolakek.pimiwidget.data.DataKeys
import com.kolakek.pimiwidget.widget.PimiWidget
import com.kolakek.pimiwidget.data.JsonDataStore
import com.kolakek.pimiwidget.location.LocationService
import com.kolakek.pimiwidget.weather.WeatherService
import com.kolakek.pimiwidget.widget.WidgetAction
import timber.log.Timber

internal object DataUpdater {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    internal suspend fun update(context: Context, forceUpdate: Boolean) {

        val lastSuccessTimeMillis = getLastSuccessTimeMillis(context)

        saveUpdateStatusData(context, UpdateStatus.RUNNING, lastSuccessTimeMillis)

        if (!hasLocationPermission(context)) {
            Timber.w("update: Location permission denied")
            saveUpdateStatusData(context, UpdateStatus.FAILED, lastSuccessTimeMillis)
            return
        }

        Timber.d("update: Get location")
        val location = LocationService.getLocation(context)

        if (location == null) {
            Timber.w("update: No location data available")
            saveUpdateStatusData(context, UpdateStatus.FAILED, lastSuccessTimeMillis)
            return
        }

        Timber.d("update: Store location data")
        JsonDataStore.save(context, DataKeys.LOCATION_DATA_KEY, location)

        Timber.d("update: Get weather data")
        val weather = WeatherService.getWeather(location)

        if (weather == null) {
            Timber.w("update: No weather data available")
            saveUpdateStatusData(context, UpdateStatus.FAILED, lastSuccessTimeMillis)
            return
        }

        Timber.d("update: Store weather data")
        JsonDataStore.save(context, DataKeys.WEATHER_DATA_KEY, weather)

        Timber.d("update: Store status data")
        val currentTimeMillis = System.currentTimeMillis()
        saveUpdateStatusData(context, UpdateStatus.SUCCESS, currentTimeMillis)

        val widgetDataAge = currentTimeMillis - (lastSuccessTimeMillis ?: 0L)
        if (forceUpdate || widgetDataAge > 120 * 60 * 1000L) { // ToDo derive const
            Timber.d("update: Update widget")
            updateWidget(context)
        }
    }

    private suspend fun getLastSuccessTimeMillis(context: Context): Long? {
        return JsonDataStore.load<UpdateStatusData?>(
            context, DataKeys.UPDATE_STATUS_DATA_KEY
        )?.successTimeMillis
    }

    private suspend fun saveUpdateStatusData(
        context: Context,
        updateStatus: UpdateStatus,
        successTimeMillis: Long?
    ) {
        JsonDataStore.save(
            context,
            DataKeys.UPDATE_STATUS_DATA_KEY,
            UpdateStatusData(
                updateStatus = updateStatus,
                statusTimeMillis = System.currentTimeMillis(),
                successTimeMillis = successTimeMillis,
                locationStatus = UpdateStatus.FAILED,
                weatherStatus = UpdateStatus.FAILED,
                lastUpdateTimeMillis = 0L

            )
        )
    }

    private fun hasLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun updateWidget(context: Context) {
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, PimiWidget::class.java))

        val intent = Intent(context, PimiWidget::class.java).apply {
            action = WidgetAction.DATA_UPDATED
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}
