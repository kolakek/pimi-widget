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
import android.content.Intent
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.DataKeys
import com.kolakek.pimiwidget.data.JsonDataStore
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.weather.WeatherData
import java.util.Locale

internal object WidgetRenderer {

    internal fun renderBaseWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int
    ): RemoteViews {
        WidgetIntent.categoryIntent(context, Intent.CATEGORY_APP_CALENDAR, appWidgetId)?.let {
            views.setOnClickPendingIntent(R.id.widget_text_clock, it)
        }
        val pendingIntent = WidgetIntent.categoryIntent(
            context,
            Intent.CATEGORY_APP_WEATHER,
            appWidgetId
        ) ?: WidgetIntent.altWeatherAppIntent(context, appWidgetId)

        pendingIntent?.let {
            views.setOnClickPendingIntent(R.id.widget_temp, it)
        }
        return views
    }

    internal fun renderDateFormat(
        context: Context,
        views: RemoteViews,
    ): RemoteViews {
        val pattern = DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            context.getString(R.string.widget_date_format)
        )
        views.setCharSequence(R.id.widget_text_clock, "setFormat12Hour", pattern)
        views.setCharSequence(R.id.widget_text_clock, "setFormat24Hour", pattern)

        return views
    }

    internal fun renderWeather(
        context: Context,
        views: RemoteViews,
        prefs: WidgetPreferences
    ): RemoteViews {
        views.setViewVisibility(R.id.widget_temp, View.INVISIBLE)

        if (prefs.showWeather) {
            JsonDataStore.loadSync<WeatherData>(
                context,
                DataKeys.WEATHER_DATA_KEY
            )?.let { data ->
                WeatherFormatter.getWidgetWeatherStrAndIcons(
                    context,
                    data,
                    prefs
                )?.let { (text, iconId1, iconId2) ->
                    views.setTextViewText(R.id.widget_temp, text)
                    views.setTextViewCompoundDrawables(R.id.widget_temp, iconId1, 0, iconId2, 0)
                    views.setViewVisibility(R.id.widget_temp, View.VISIBLE)
                }
            }
        }
        return views
    }

    internal fun getWidgetLayout(textStyle: TextStyle): Int {
        return when (textStyle) {
            TextStyle.DARK -> R.layout.pimi_widget_dark
            TextStyle.LIGHT -> R.layout.pimi_widget_light
            TextStyle.LIGHT_SHADOW -> R.layout.pimi_widget_light_shadow
        }
    }
}
