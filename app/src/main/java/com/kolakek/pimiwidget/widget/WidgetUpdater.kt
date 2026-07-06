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

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.resources.WidgetIcon
import com.kolakek.pimiwidget.settings.AuxDisplay
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.settings.TextStyle
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.utility.WeatherApp
import com.kolakek.pimiwidget.weather.WeatherData
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.Date
import java.util.Locale

internal object WidgetUpdater {

    internal fun updateWidgets(context: Context) {
        Timber.d("WidgetUpdater: updateWidgets")

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, PimiWidget::class.java)
        )
        val prefs = PreferencesHelper.getWidgetPreferences(context)
        val weatherData = if (prefs.showWeather) {
            runBlocking { DataRepository.loadWeatherData(context) }
        } else null

        for (appWidgetId in appWidgetIds) {

            val views = RemoteViews(
                context.packageName,
                getWidgetLayout(prefs.textStyle)
            )
            updateBaseWidget(context, views, appWidgetId, prefs)
            updateAlarm(context, views, prefs)
            updateWeather(context, views, prefs, weatherData)
            updateAuxDisplay(context, views, prefs)
            updateVisibility(context, views, appWidgetId)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    internal fun partiallyUpdateWidgets(
        context: Context,
        prefs: WidgetPreferences,
        weatherData: WeatherData?
    ): UpdateWidgetStatus {
        Timber.d("WidgetUpdater: partiallyUpdateWidgets")

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, PimiWidget::class.java)
        )
        val status = UpdateWidgetStatus(
            weatherUpdate = UpdateWeatherStatus.DONE
        )
        for (appWidgetId in appWidgetIds) {

            val views = RemoteViews(
                context.packageName,
                getWidgetLayout(prefs.textStyle)
            )
            status.weatherUpdate = updateWeather(context, views, prefs, weatherData)
            updateAuxDisplay(context, views, prefs)
            updateAlarm(context, views, prefs)

            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }
        return status
    }

    internal fun partiallyUpdateAlarms(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, PimiWidget::class.java)
        )
        val prefs = PreferencesHelper.getWidgetPreferences(context)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(
                context.packageName,
                getWidgetLayout(prefs.textStyle)
            )
            updateAlarm(context, views, prefs)
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }
    }

    internal fun partiallyUpdateVisibility(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
        val prefs = PreferencesHelper.getWidgetPreferences(context)
        val views = RemoteViews(
            context.packageName,
            getWidgetLayout(prefs.textStyle)
        )
        updateVisibility(context, views, appWidgetId)
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
    }

    private fun getWidgetLayout(textStyle: TextStyle): Int {
        return when (textStyle) {
            TextStyle.DARK -> R.layout.pimi_widget_dark
            TextStyle.LIGHT -> R.layout.pimi_widget_light
            TextStyle.LIGHT_SHADOW -> R.layout.pimi_widget_light_shadow
        }
    }

    private fun updateBaseWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        prefs: WidgetPreferences
    ) {
        val pattern = DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            context.getString(R.string.widget_date_format)
        )
        views.setCharSequence(R.id.widget_text_clock, "setFormat12Hour", pattern)
        views.setCharSequence(R.id.widget_text_clock, "setFormat24Hour", pattern)

        views.setOnClickPendingIntent(R.id.widget_root, null)

        val calendarIntent = WidgetIntent.categoryIntent(
            context,
            Intent.CATEGORY_APP_CALENDAR,
            appWidgetId
        )
        views.setOnClickPendingIntent(R.id.widget_text_clock, calendarIntent)

        val weatherAppIntent = if (prefs.weatherApp == WeatherApp.NONE) {
            null
        } else {
            WidgetIntent.appIntent(context, appWidgetId, prefs.weatherApp.packageName)
        }
        views.setOnClickPendingIntent(R.id.widget_temp, weatherAppIntent)

        views.setOnClickPendingIntent(
            R.id.widget_alarm,
            WidgetIntent.actionIntent(context, AlarmClock.ACTION_SHOW_ALARMS, appWidgetId)
        )
    }

    private fun updateAlarm(
        context: Context,
        views: RemoteViews,
        prefs: WidgetPreferences
    ) {
        views.setTextViewText(R.id.widget_alarm, null)
        views.setTextViewCompoundDrawables(R.id.widget_alarm, 0, 0, 0, 0)

        if (!prefs.showAlarms) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarmMillis = alarmManager.nextAlarmClock?.triggerTime ?: return

        if (nextAlarmMillis - System.currentTimeMillis() > ALARM_LOOK_AHEAD_MILLIS) return

        views.setTextViewText(
            R.id.widget_alarm,
            DateFormat.getTimeFormat(context).format(Date(nextAlarmMillis))
        )
        views.setTextViewCompoundDrawables(
            R.id.widget_alarm,
            WidgetIcon.ALARM.id(prefs.textStyle),
            0,
            0,
            0
        )
    }

    private fun updateVisibility(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int
    ) {
        val viewHeight = context.resources.getDimensionPixelSize(R.dimen.widget_date_height) +
                context.resources.getDimensionPixelSize(R.dimen.widget_weather_height) +
                context.resources.getDimensionPixelSize(R.dimen.widget_aux_height) +
                context.resources.getDimensionPixelSize(R.dimen.widget_spacer_height)

        val widgetHeight = AppWidgetManager
            .getInstance(context)
            .getAppWidgetOptions(appWidgetId)
            .getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            .dpToPx(context)

        if (viewHeight > widgetHeight) views.setViewVisibility(R.id.widget_aux, View.INVISIBLE)
        else views.setViewVisibility(R.id.widget_aux, View.VISIBLE)
    }

    private fun updateWeather(
        context: Context,
        views: RemoteViews,
        prefs: WidgetPreferences,
        weatherData: WeatherData?
    ): UpdateWeatherStatus {

        views.setTextViewText(R.id.widget_temp, null)
        views.setTextViewCompoundDrawables(R.id.widget_temp, 0, 0, 0, 0)

        if (!prefs.showWeather) return UpdateWeatherStatus.DONE

        weatherData?.let { data ->
            WeatherRenderer.getWidgetWeatherStrAndIcons(context, data, prefs)?.let {

                views.setTextViewText(R.id.widget_temp, it.text)
                views.setTextViewCompoundDrawables(R.id.widget_temp, it.iconId1, 0, it.iconId2, 0)

            } ?: return UpdateWeatherStatus.NEEDS_DATA_AND_REFRESH

            val isFresh = System.currentTimeMillis() - data.timeMillis < WEATHER_UPDATE_AGE_MILLIS
            return if (isFresh) UpdateWeatherStatus.DONE else UpdateWeatherStatus.NEEDS_DATA
        }
        return UpdateWeatherStatus.NEEDS_DATA_AND_REFRESH
    }

    private fun updateAuxDisplay(
        context: Context,
        views: RemoteViews,
        prefs: WidgetPreferences
    ) {
        views.setTextViewText(R.id.widget_aux, null)

        if (!prefs.showWeather) return

        val auxStr = when (prefs.auxDisplay) {

            AuxDisplay.NOTHING ->
                return

            AuxDisplay.UPDATE_TIME ->
                DateFormat.getTimeFormat(context).format(Date(System.currentTimeMillis()))
        }
        views.setTextViewText(
            R.id.widget_aux,
            context.getString(R.string.widget_updated_at) + " $auxStr"
        )
    }

    private fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}
