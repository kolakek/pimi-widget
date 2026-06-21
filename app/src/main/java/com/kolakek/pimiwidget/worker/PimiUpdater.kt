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
import androidx.annotation.RequiresPermission
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.location.LocationService
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.weather.WeatherService
import com.kolakek.pimiwidget.widget.DATA_UPDATE_INTERVAL_MILLIS
import com.kolakek.pimiwidget.widget.WidgetUpdateStatus
import com.kolakek.pimiwidget.widget.WidgetUpdater

internal object PimiUpdater {

    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    internal suspend fun update(context: Context) {

        val prefs = PreferencesHelper.getWidgetPreferences(context)

        val weatherData = if (prefs.showWeather) {
            DataRepository.loadWeatherData(context)
        } else null

        val status = WidgetUpdater.partiallyUpdateWidgets(context, prefs, weatherData)

        if (!prefs.showWeather) return

        val dataTimeMillis = weatherData?.timeMillis ?: 0
        val dataAgeMillis = System.currentTimeMillis() - dataTimeMillis

        val isDataFresh = dataAgeMillis < DATA_UPDATE_INTERVAL_MILLIS
        val isDataValid = status == WidgetUpdateStatus.SUCCESS

        if (isDataValid && isDataFresh) return

        val location = LocationService.fetchLocation(context)
        WeatherService.fetchWeatherData(context, location)
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
}
