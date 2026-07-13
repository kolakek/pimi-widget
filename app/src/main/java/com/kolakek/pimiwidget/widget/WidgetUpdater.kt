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

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.settings.TextStyle
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.weather.WeatherData
import kotlinx.coroutines.runBlocking
import timber.log.Timber

object WidgetUpdater {

    fun updateWidgets(context: Context) {
        Timber.d("WidgetUpdater: updateWidgets")

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, PimiWidget::class.java)
        )
        val prefs = PreferencesHelper.getWidgetPreferences(context)

        val weatherData = if (prefs.showWeather) {
            runBlocking { DataRepository.loadWeatherData(context) }
        } else null

        val birthdayData = if (prefs.showBirthdays) {
            runBlocking { DataRepository.loadBirthdayData(context) }
        } else null

        for (appWidgetId in appWidgetIds) {

            val views = RemoteViews(
                context.packageName,
                getWidgetLayout(prefs.textStyle)
            )
            CoreUpdater.updateViews(context, views, appWidgetId, prefs)
            AlarmUpdater.updateViews(context, views, prefs)
            WeatherUpdater.updateViews(context, views, prefs, weatherData)
            val birthdayStatus = BirthdayUpdater.updateViews(context, views, prefs, birthdayData)
            AuxUpdater.updateViews(context, views, prefs)
            VisibilityUpdater.updateEventViews(views, birthdayStatus)
            VisibilityUpdater.updateAuxViews(context, views, appWidgetId)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    fun refreshWidgetData(
        context: Context,
        prefs: WidgetPreferences,
        weatherData: WeatherData?
    ): WeatherUpdateStatus {
        Timber.d("WidgetUpdater: refreshWidgetData")

        var lastStatus = WeatherUpdateStatus.DONE

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, PimiWidget::class.java)
        )
        for (appWidgetId in appWidgetIds) {

            val views = RemoteViews(
                context.packageName,
                getWidgetLayout(prefs.textStyle)
            )
            lastStatus = WeatherUpdater.updateViews(context, views, prefs, weatherData)

            AuxUpdater.updateViews(context, views, prefs)
            AlarmUpdater.updateViews(context, views, prefs)

            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }
        return lastStatus
    }

    fun refreshAlarm(context: Context) {
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
            AlarmUpdater.updateViews(context, views, prefs)

            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }
    }

    fun refreshBirthday(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, PimiWidget::class.java)
        )
        val prefs = PreferencesHelper.getWidgetPreferences(context)
        val birthdayData = if (prefs.showBirthdays) {
            runBlocking { DataRepository.loadBirthdayData(context) }
        } else null

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(
                context.packageName,
                getWidgetLayout(prefs.textStyle)
            )
            val birthdayStatus = BirthdayUpdater.updateViews(context, views, prefs, birthdayData)
            VisibilityUpdater.updateEventViews(views, birthdayStatus)

            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }
    }

    fun refreshVisibility(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
        val prefs = PreferencesHelper.getWidgetPreferences(context)
        val views = RemoteViews(
            context.packageName,
            getWidgetLayout(prefs.textStyle)
        )
        VisibilityUpdater.updateAuxViews(context, views, appWidgetId)

        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
    }

    private fun getWidgetLayout(textStyle: TextStyle): Int {
        return when (textStyle) {
            TextStyle.DARK, TextStyle.LIGHT -> R.layout.widget
            TextStyle.LIGHT_SHADOW -> R.layout.widget_shadow
        }
    }
}
