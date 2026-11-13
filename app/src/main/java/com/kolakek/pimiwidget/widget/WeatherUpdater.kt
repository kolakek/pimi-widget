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
import com.kolakek.pimiwidget.weather.WarningLevel
import com.kolakek.pimiwidget.weather.WeatherData
import com.kolakek.pimiwidget.weather.WeatherRenderer

object WeatherUpdater {

    fun updateViews(
        context: Context,
        views: RemoteViews,
        settings: WidgetSettings,
        weatherData: WeatherData?
    ): WeatherUpdateStatus {

        views.setViewVisibility(R.id.widget_weather_icon, View.GONE)
        views.setViewVisibility(R.id.widget_temp_text, View.GONE)
        views.setViewVisibility(R.id.widget_sync_icon, View.GONE)
        views.setViewVisibility(R.id.widget_aux, View.GONE)
        views.setViewVisibility(R.id.widget_warn_svr_icon, View.GONE)
        views.setViewVisibility(R.id.widget_warn_xtr_icon, View.GONE)

        if (!settings.showWeather) return WeatherUpdateStatus.NO_ACTION_NEEDED

        views.setViewVisibility(R.id.widget_sync_icon, View.VISIBLE)

        weatherData?.let { data ->
            WeatherRenderer.currentWeather(
                context,
                data,
                settings.tempUnit,
                settings.iconStyle,
                settings.iconColor,
                settings.showTempUnit
            )?.let {
                views.setViewVisibility(R.id.widget_sync_icon, View.GONE)
                views.setViewVisibility(R.id.widget_weather_icon, View.VISIBLE)
                views.setViewVisibility(R.id.widget_temp_text, View.VISIBLE)

                views.setImageViewResource(R.id.widget_weather_icon, it.iconId)
                views.setTextViewText(R.id.widget_temp_text, it.text)

                var auxStr: String? = null
                var warnLevel: WarningLevel? = null

                if (settings.showWeatherWarning) {
                    auxStr = WeatherRenderer.currentWarningStr(context, data)
                    warnLevel = WeatherRenderer.currentWarningLevel(data)
                }
                if (auxStr == null && settings.showDailyForecast) {
                    auxStr = WeatherRenderer.forecastString(context, data, settings)
                }
                if (auxStr == null) {
                    auxStr = WeatherRenderer.auxString(context, data, settings)
                }
                auxStr?.let {
                    views.setViewVisibility(R.id.widget_aux, View.VISIBLE)
                    views.setTextViewText(R.id.widget_aux_text, auxStr)
                }
                when (warnLevel) {
                    WarningLevel.NONE -> {}

                    WarningLevel.SEVERE ->
                        views.setViewVisibility(R.id.widget_warn_svr_icon, View.VISIBLE)

                    WarningLevel.EXTREME ->
                        views.setViewVisibility(R.id.widget_warn_xtr_icon, View.VISIBLE)

                    null -> {}
                }
            } ?: return WeatherUpdateStatus.NEEDS_DATA_AND_REFRESH

            val isFresh = System.currentTimeMillis() - data.timeMillis < WEATHER_UPDATE_AGE_MILLIS

            return if (isFresh) WeatherUpdateStatus.NO_ACTION_NEEDED
            else WeatherUpdateStatus.NEEDS_DATA
        }
        return WeatherUpdateStatus.NEEDS_DATA_AND_REFRESH
    }
}
