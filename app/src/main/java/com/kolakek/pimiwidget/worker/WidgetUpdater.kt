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

object WidgetUpdater {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    internal suspend fun update(context: Context) {
        Timber.d("update: Start widget update")

        val location = if (hasLocationPermission(context)) {
            LocationService.getLocation(context)
        } else {
            Timber.w("Location permission denied")
            null
        }
        val weather = if (location != null) {
            WeatherService.getWeather(location)
        } else {
            Timber.w("No location available")
            null
        }
        if (weather != null) {
            JsonDataStore.save(context, DataKeys.DATA_WEATHER_KEY, weather)
        } else {
            Timber.w("No weather data available")
        }
        JsonDataStore.save(
            context,
            DataKeys.DATA_DEBUG_KEY,
            DebugData(
                location == null,
                weather == null,
                System.currentTimeMillis()
            )
        )
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
