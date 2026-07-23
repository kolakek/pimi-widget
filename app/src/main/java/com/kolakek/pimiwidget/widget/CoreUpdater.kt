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
import android.graphics.Color
import android.provider.AlarmClock
import android.text.format.DateFormat
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.settings.TextColor
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.settings.WidgetStyle
import com.kolakek.pimiwidget.utility.WeatherApp
import java.util.Locale

object CoreUpdater {

    fun updateViews(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        prefs: WidgetPreferences
    ) {
        val pattern = DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            context.getString(R.string.widget_date_format)
        )
        views.setCharSequence(R.id.widget_date, "setFormat12Hour", pattern)
        views.setCharSequence(R.id.widget_date, "setFormat24Hour", pattern)

        views.setOnClickPendingIntent(R.id.widget_root, null)

        val calendarIntent = WidgetIntent.categoryIntent(
            context,
            Intent.CATEGORY_APP_CALENDAR,
            appWidgetId
        )
        views.setOnClickPendingIntent(R.id.widget_date, calendarIntent)

        val weatherAppIntent = if (prefs.weatherApp == WeatherApp.NONE) {
            null
        } else {
            WidgetIntent.appIntent(context, appWidgetId, prefs.weatherApp.packageName)
        }
        views.setOnClickPendingIntent(R.id.widget_weather_icon, weatherAppIntent)
        views.setOnClickPendingIntent(R.id.widget_weather_temp, weatherAppIntent)

        views.setOnClickPendingIntent(
            R.id.widget_alarm,
            WidgetIntent.actionIntent(context, AlarmClock.ACTION_SHOW_ALARMS, appWidgetId)
        )
        views.setOnClickPendingIntent(
            R.id.widget_birthday,
            WidgetIntent.birthdayDismissIntent(context, appWidgetId)
        )
        val textColor = when (prefs.textColor) {
            TextColor.LIGHT -> Color.WHITE
            TextColor.DARK -> Color.BLACK
            TextColor.THEMED -> context.getColor(R.color.dynamic_text_color)
        }
        views.setTextColor(R.id.widget_date, textColor)
        views.setTextColor(R.id.widget_alarm, textColor)
        views.setTextColor(R.id.widget_birthday, textColor)
        views.setTextColor(R.id.widget_weather_aux, textColor)

        if (prefs.widgetStyle == WidgetStyle.SOLID && prefs.textColor == TextColor.THEMED) {
            val tempColor = context.getColor(R.color.dynamic_temp_color)
            views.setTextColor(R.id.widget_weather_temp, tempColor)
        } else {
            views.setTextColor(R.id.widget_weather_temp, textColor)
        }
    }
}
