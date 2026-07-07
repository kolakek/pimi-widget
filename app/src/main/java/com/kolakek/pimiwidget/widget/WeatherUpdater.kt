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

package com.kolakek.pimiwidget.widget

import android.content.Context
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.weather.WeatherData

object WeatherUpdater {

    fun updateViews(
        context: Context,
        views: RemoteViews,
        prefs: WidgetPreferences,
        weatherData: WeatherData?
    ): WeatherUpdateStatus {

        views.setTextViewText(R.id.widget_temp, null)
        views.setTextViewCompoundDrawables(R.id.widget_temp, 0, 0, 0, 0)

        if (!prefs.showWeather) return WeatherUpdateStatus.DONE

        weatherData?.let { data ->
            WeatherRenderer.getWidgetWeatherStrAndIcons(context, data, prefs)?.let {

                views.setTextViewText(R.id.widget_temp, it.text)
                views.setTextViewCompoundDrawables(R.id.widget_temp, it.iconId1, 0, it.iconId2, 0)

            } ?: return WeatherUpdateStatus.NEEDS_DATA_AND_REFRESH

            val isFresh = System.currentTimeMillis() - data.timeMillis < WEATHER_UPDATE_AGE_MILLIS
            return if (isFresh) WeatherUpdateStatus.DONE else WeatherUpdateStatus.NEEDS_DATA
        }
        return WeatherUpdateStatus.NEEDS_DATA_AND_REFRESH
    }
}
