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
import timber.log.Timber

internal object WidgetUpdater {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    internal suspend fun update(context: Context) {
        Timber.d("update: Get location")
        val location = if (hasLocationPermission(context)) {
            LocationService.getLocation(context)
        } else {
            Timber.w("update: Location permission denied")
            null
        }
        Timber.d("update: Get weather data")
        val weather = if (location != null) {
            WeatherService.getWeather(location)
        } else {
            Timber.w("update: No location available")
            null
        }
        if (weather != null) {
            Timber.d("update: Store weather data")
            JsonDataStore.save(context, DataKeys.WEATHER_DATA_KEY, weather)
        } else {
            Timber.w("update: No weather data available")
        }
        JsonDataStore.save(
            context,
            DataKeys.WORKER_STATUS_DATA_KEY,
            WorkerStatusData(
                location != null,
                weather != null,
                System.currentTimeMillis()
            )
        )
        Timber.d("update: Update widget")
        updateWidget(context)
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
            action = "com.kolakek.pimiwidget.action.WEATHER_UPDATE"
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}
