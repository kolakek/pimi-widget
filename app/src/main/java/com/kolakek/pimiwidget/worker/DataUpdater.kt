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
import androidx.annotation.RequiresPermission
import com.kolakek.pimiwidget.data.DataKeys
import com.kolakek.pimiwidget.widget.PimiWidget
import com.kolakek.pimiwidget.data.JsonDataStore
import com.kolakek.pimiwidget.location.LocationService
import com.kolakek.pimiwidget.weather.WeatherData
import com.kolakek.pimiwidget.weather.WeatherService
import com.kolakek.pimiwidget.widget.WidgetAction
import timber.log.Timber

internal object DataUpdater {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    internal suspend fun update(context: Context) {

        Timber.d("update: Get location")
        val location = LocationService.getLocation(context)

        JsonDataStore.save(context, DataKeys.LOCATION_DATA_KEY, location)

        Timber.d("update: Get weather data")
        val weather = WeatherService.getWeather(location)

        val widgetDataAgeMillis = getDataAgeMillis(context)

        JsonDataStore.save(context, DataKeys.WEATHER_DATA_KEY, weather)

        if (widgetDataAgeMillis > WIDGET_DATA_MAX_AGE_MILLIS) {
            Timber.d("update: Trigger widget update")
            triggerWidgetUpdate(context)
        }
    }

    internal suspend fun logUpdateStatus(
        context: Context,
        updateStatus: String,
    ) {
        JsonDataStore.save(
            context,
            DataKeys.STATUS_DATA_KEY,
            StatusData(updateStatus, System.currentTimeMillis())
        )
    }

    private suspend fun getDataAgeMillis(context: Context): Long {
        val dataTimeMillis = JsonDataStore.load<WeatherData?>(
            context, DataKeys.WEATHER_DATA_KEY
        )?.timeMillis ?: 0
        return System.currentTimeMillis() - dataTimeMillis
    }

    private fun triggerWidgetUpdate(context: Context) {
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, PimiWidget::class.java))

        val intent = Intent(context, PimiWidget::class.java).apply {
            action = WidgetAction.APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}
