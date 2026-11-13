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
import android.provider.AlarmClock
import android.text.format.DateFormat
import android.widget.RemoteViews
import androidx.annotation.ColorRes
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.settings.WidgetPreferences
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
        views.setCharSequence(R.id.widget_date_text, "setFormat12Hour", pattern)
        views.setCharSequence(R.id.widget_date_text, "setFormat24Hour", pattern)

        views.setOnClickPendingIntent(R.id.widget_root, null)

        val calendarIntent = WidgetIntent.categoryIntent(
            context,
            Intent.CATEGORY_APP_CALENDAR,
            appWidgetId
        )
        views.setOnClickPendingIntent(R.id.widget_date_text, calendarIntent)

        val weatherAppIntent = if (prefs.weatherApp == WeatherApp.PIMI) {
            WidgetIntent.pimiWeatherIntent(context, appWidgetId)
        } else {
            WidgetIntent.appIntent(context, appWidgetId, prefs.weatherApp.packageName)
        }
        views.setOnClickPendingIntent(R.id.widget_weather, weatherAppIntent)

        views.setOnClickPendingIntent(
            R.id.widget_alarm,
            WidgetIntent.actionIntent(context, AlarmClock.ACTION_SHOW_ALARMS, appWidgetId)
        )
        views.setOnClickPendingIntent(
            R.id.widget_birthday,
            WidgetIntent.birthdayDismissIntent(context, appWidgetId)
        )
        val textColor = prefs.widgetStyle.textColor(prefs.textColor)
        views.setTextColorRes(R.id.widget_date_text, textColor)
        views.setTextColorRes(R.id.widget_alarm_text, textColor)
        views.setTextColorRes(R.id.widget_birthday_text, textColor)
        views.setTextColorRes(R.id.widget_aux_text, textColor)

        val tempColor = prefs.widgetStyle.tempTextColor(prefs.textColor)
        views.setTextColorRes(R.id.widget_temp_text, tempColor)

        if (!prefs.widgetStyle.needsShadow) {
            views.setImageColorRes(R.id.widget_sync_icon, tempColor)
            views.setImageColorRes(R.id.widget_alarm_icon, textColor)
            views.setImageColorRes(R.id.widget_birthday_icon, textColor)
        }
        if (prefs.widgetStyle.hasBackground) {
            views.setImageAlpha(R.id.widget_background, prefs.backgroundAlpha)
            prefs.widgetStyle.backgroundColor()?.let {
                views.setImageColorRes(R.id.widget_background, it)
            }
            prefs.widgetStyle.iconBackgroundColor()?.let {
                views.setImageColorRes(R.id.widget_icon_background, it)
            }
        }
    }

    private fun RemoteViews.setTextColorRes(viewId: Int, @ColorRes colorRes: Int) =
        setColor(viewId, "setTextColor", colorRes)

    private fun RemoteViews.setImageColorRes(viewId: Int, @ColorRes colorRes: Int) =
        setColor(viewId, "setColorFilter", colorRes)

    private fun RemoteViews.setImageAlpha(viewId: Int, alpha: Int) =
        setInt(viewId, "setImageAlpha", alpha)
}
