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

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.DataKeys
import com.kolakek.pimiwidget.data.JsonDataStore
import com.kolakek.pimiwidget.weather.WeatherData
import timber.log.Timber
import java.util.Locale

internal object WidgetRenderer {

    internal fun updateBaseWidget(
        context: Context,
        views: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        WidgetIntents.categoryIntent(context, Intent.CATEGORY_APP_CALENDAR, appWidgetId)?.let {
            views.setOnClickPendingIntent(R.id.widget_text_clock, it)
        }
        val pendingIntent = WidgetIntents.categoryIntent(context, Intent.CATEGORY_APP_WEATHER, appWidgetId)
            ?: WidgetIntents.altWeatherAppIntent(context, appWidgetId)

        pendingIntent?.let {
            views.setOnClickPendingIntent(R.id.widget_temp, it)
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    internal fun updateDate(
        context: Context,
        views: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val pattern = DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            context.getString(R.string.widget_date_format)
        )
        Timber.d("updateDate: Set pattern $pattern")

        views.setCharSequence(R.id.widget_text_clock, "setFormat12Hour", pattern)
        views.setCharSequence(R.id.widget_text_clock, "setFormat24Hour", pattern)

        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
    }

    internal fun updateWeather(
        context: Context,
        views: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        prefs: WidgetPreferences
    ) {
        Timber.d("updateWeather: Update weather display")
        views.setViewVisibility(R.id.widget_temp, View.INVISIBLE)

        if (prefs.showWeather) {
            val weatherData: WeatherData? = JsonDataStore.loadSync(
                context,
                DataKeys.WEATHER_DATA_KEY
            )
            weatherData?.let { data ->
                Timber.d("updateWeather: Weather data available")

                val timeMillis = System.currentTimeMillis()
                val weatherStrIcon = WeatherFormatter.getCurrentWeatherStrAndIcon(
                    context,
                    data,
                    timeMillis,
                    prefs.tempUnit,
                    prefs.iconStyle,
                    useLightText(context, prefs.textColor)
                )
                weatherStrIcon?.let {
                    Timber.d("updateWeather: Valid weather data found")

                    val forecastStr = if (prefs.showForecast) {
                        WeatherFormatter.getForecastStr(
                            context,
                            timeMillis,
                            data,
                            prefs.tempUnit
                        ) ?: ""
                    } else ""

                    views.setTextViewText(
                        R.id.widget_temp,
                        it.text + forecastStr
                    )
                    views.setTextViewCompoundDrawables(
                        R.id.widget_temp,
                        it.iconId,
                        0,
                        0,
                        0
                    )
                    views.setViewVisibility(R.id.widget_temp, View.VISIBLE)
                }
            }
        }
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
    }

    internal fun buildRemoteViews(
        context: Context,
        prefs: WidgetPreferences
    ): RemoteViews {
        val lightText = useLightText(context, prefs.textColor)

        val layout = when {
            lightText && prefs.iconStyle == KEY_ICON_STYLE_OUTLINED ->
                R.layout.pimi_widget_light

            lightText ->
                R.layout.pimi_widget_light_shadow

            else ->
                R.layout.pimi_widget_dark
        }
        return RemoteViews(context.packageName, layout)
    }

    private fun useLightText(context: Context, textColor: String): Boolean =
        when (textColor) {
            KEY_COLOR_AUTO -> WallpaperManager.getInstance(context)
                .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                ?.colorHints
                ?.let { it and WallpaperColors.HINT_SUPPORTS_DARK_TEXT == 0 } ?: true

            KEY_COLOR_LIGHT -> true
            else -> false
        }
}
