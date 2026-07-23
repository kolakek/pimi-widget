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
import android.view.View
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.settings.WidgetStyle
import com.kolakek.pimiwidget.weather.WeatherData

object WeatherUpdater {

    fun updateViews(
        context: Context,
        views: RemoteViews,
        prefs: WidgetPreferences,
        weatherData: WeatherData?
    ): WeatherUpdateStatus {

        views.setImageViewResource(R.id.widget_weather_icon, 0)
        views.setTextViewText(R.id.widget_weather_temp, null)
        views.setTextViewText(R.id.widget_weather_aux, null)
        views.setTextViewCompoundDrawables(R.id.widget_weather_aux, 0, 0, 0, 0)
        views.setViewVisibility(R.id.widget_weather_aux, View.GONE)

        if (!prefs.showWeather) return WeatherUpdateStatus.HAS_RECENT_DATA

        weatherData?.let { data ->
            val nowTimeMillis = System.currentTimeMillis()

            WeatherRenderer.getCurrentWeather(context, data, nowTimeMillis, prefs)?.let { it ->

                views.setImageViewResource(R.id.widget_weather_icon, it.iconId)
                views.setTextViewText(R.id.widget_weather_temp, it.text)

                var auxStr: String? = null
                var auxIcon = 0

                if (prefs.showWeatherWarning) {
                    WeatherRenderer.getWarning(context, nowTimeMillis, data, prefs)?.let{
                        auxStr = it.text
                        auxIcon = it.iconId
                    }
                }
                if (auxStr == null && prefs.showDailyForecast) {
                    WeatherRenderer.getForecast(context, nowTimeMillis, data, prefs)?.let {
                        auxStr = it
                    }
                }
                if (auxStr == null) {
                    WeatherRenderer.getAuxInfo(context, nowTimeMillis, prefs)?.let {
                        auxStr = it
                    }
                }
                val sep = if (prefs.widgetStyle == WidgetStyle.SOLID) "" else " · "
                auxStr = auxStr?.prependIndent(sep)

                auxStr?.let {
                    views.setViewVisibility(R.id.widget_weather_aux, View.VISIBLE)
                    views.setTextViewText(R.id.widget_weather_aux, auxStr)
                    views.setTextViewCompoundDrawables(R.id.widget_weather_aux, 0, 0, auxIcon, 0)
                }

            } ?: return WeatherUpdateStatus.HAS_EXPIRED_DATA

            val isFresh = System.currentTimeMillis() - data.timeMillis < WEATHER_UPDATE_AGE_MILLIS

            return if (isFresh) WeatherUpdateStatus.HAS_RECENT_DATA
            else WeatherUpdateStatus.HAS_STALE_DATA
        }
        return WeatherUpdateStatus.HAS_EXPIRED_DATA
    }
}
