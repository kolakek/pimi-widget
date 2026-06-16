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
import com.kolakek.pimiwidget.weather.WeatherService
import com.kolakek.pimiwidget.widget.WidgetUpdater
import timber.log.Timber

internal object DataUpdater {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    internal suspend fun update(context: Context) {
        val location = LocationService.fetchLocation(context)
        val widgetDataAgeMillis = getDataAgeMillis(context)

        WeatherService.fetchWeatherData(context, location)

        if (widgetDataAgeMillis > WIDGET_DATA_MAX_AGE_MILLIS) {
            Timber.d("DataUpdater: Trigger widget update")
            WidgetUpdater.updateWidgets(context, DataRepository.loadWeatherData(context))
        }
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

    private suspend fun getDataAgeMillis(context: Context): Long {
        val dataTimeMillis = DataRepository.loadWeatherData(context)?.timeMillis ?: 0
        return System.currentTimeMillis() - dataTimeMillis
    }
}
