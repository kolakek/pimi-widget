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
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.weather.WeatherData
import java.util.Locale

internal object WidgetRenderer {

    internal fun updateBaseWidget(
        context: Context,
        views: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
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
        views.setViewVisibility(R.id.widget_temp, View.INVISIBLE)

        if (prefs.showWeather) {
            JsonDataStore.loadSync<WeatherData>(
                context,
                DataKeys.WEATHER_DATA_KEY
            )?.let { data ->
                WeatherFormatter.getWidgetWeatherStrAndIcons(
                    context,
                    data,
                    showWarning = true, // ToDo
                    prefs.showForecast,
                    prefs.tempUnit,
                    prefs.iconStyle,
                    useLightText(context, prefs.textColor)
                )?.let { (text, iconId1, iconId2) ->
                    views.setTextViewText(R.id.widget_temp, text)
                    views.setTextViewCompoundDrawables(R.id.widget_temp, iconId1, 0, iconId2, 0)
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
        val layout = if (useLightText(context, prefs.textColor)) {
            if (prefs.iconStyle == PreferencesHelper.IconStyle.FLAT_SKETCH) {
                R.layout.pimi_widget_light
            } else {
                R.layout.pimi_widget_light_shadow
            }
        } else {
            R.layout.pimi_widget_dark
        }
        return RemoteViews(context.packageName, layout)
    }

    private fun useLightText(context: Context, textColor: PreferencesHelper.TextColor): Boolean =
        when (textColor) {
            PreferencesHelper.TextColor.AUTO ->
                WallpaperManager
                    .getInstance(context)
                    .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                    ?.colorHints
                    ?.let { it and WallpaperColors.HINT_SUPPORTS_DARK_TEXT == 0 } ?: true

            PreferencesHelper.TextColor.LIGHT -> true
            PreferencesHelper.TextColor.DARK -> false
        }
}