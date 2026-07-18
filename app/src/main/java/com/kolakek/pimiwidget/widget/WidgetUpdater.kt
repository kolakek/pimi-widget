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
import com.kolakek.pimiwidget.birthday.BirthdayData
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.settings.WidgetStyle
import com.kolakek.pimiwidget.weather.WeatherData
import kotlinx.coroutines.runBlocking

object WidgetUpdater {

    fun updateWidgets(context: Context) {
        val prefs = PreferencesHelper.getWidgetPreferences(context)

        val weatherData = if (prefs.showWeather) {
            runBlocking { DataRepository.loadWeatherData(context) }
        } else null

        val birthdayData = if (prefs.showBirthdays) {
            runBlocking { DataRepository.loadBirthdayData(context) }
        } else null

        updateViews(context, prefs, partialUpdate = false) { views, appWidgetId ->
            CoreUpdater.updateViews(context, views, appWidgetId, prefs)
            AlarmUpdater.updateViews(context, views, prefs)
            WeatherUpdater.updateViews(context, views, prefs, weatherData)
            val birthdayStatus = BirthdayUpdater.updateViews(context, views, prefs, birthdayData)
            AuxUpdater.updateViews(context, views, prefs)
            VisibilityUpdater.updateEventViews(views, birthdayStatus)
            VisibilityUpdater.updateAuxViews(context, views, appWidgetId, prefs)
        }
    }

    fun refreshWidgets(
        context: Context,
        prefs: WidgetPreferences,
        weatherData: WeatherData?,
        birthdayData: BirthdayData?
    ): WeatherUpdateStatus {
        var lastWeatherStatus = WeatherUpdateStatus.HAS_RECENT_DATA

        updateViews(context, prefs, partialUpdate = true) { views, _ ->
            lastWeatherStatus = WeatherUpdater.updateViews(context, views, prefs, weatherData)
            val birthdayStatus = BirthdayUpdater.updateViews(context, views, prefs, birthdayData)
            AuxUpdater.updateViews(context, views, prefs)
            AlarmUpdater.updateViews(context, views, prefs)
            VisibilityUpdater.updateEventViews(views, birthdayStatus)
        }
        return lastWeatherStatus
    }

    fun refreshWeather(
        context: Context,
        prefs: WidgetPreferences,
        weatherData: WeatherData?
    ) {
        updateViews(context, prefs, partialUpdate = true) { views, _ ->
            WeatherUpdater.updateViews(context, views, prefs, weatherData)
            AuxUpdater.updateViews(context, views, prefs)
        }
    }

    fun refreshAlarms(context: Context) {
        val prefs = PreferencesHelper.getWidgetPreferences(context)
        updateViews(context, prefs, partialUpdate = true) { views, _ ->
            AlarmUpdater.updateViews(context, views, prefs)
        }
    }

    fun refreshBirthdays(context: Context) {
        val prefs = PreferencesHelper.getWidgetPreferences(context)
        val birthdayData = if (prefs.showBirthdays) {
            runBlocking { DataRepository.loadBirthdayData(context) }
        } else null

        refreshBirthdays(context, prefs, birthdayData)
    }

    fun refreshBirthdays(
        context: Context,
        prefs: WidgetPreferences,
        birthdayData: BirthdayData?
    ) {
        updateViews(context, prefs, partialUpdate = true) { views, _ ->
            val birthdayStatus = BirthdayUpdater.updateViews(context, views, prefs, birthdayData)
            VisibilityUpdater.updateEventViews(views, birthdayStatus)
        }
    }

    fun refreshVisibility(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = PreferencesHelper.getWidgetPreferences(context)
        val views = RemoteViews(
            context.packageName,
            getWidgetLayout(prefs.widgetStyle)
        )
        VisibilityUpdater.updateAuxViews(context, views, appWidgetId, prefs)

        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
    }

    private fun getWidgetLayout(widgetStyle: WidgetStyle): Int {
        return when (widgetStyle) {
            WidgetStyle.DEFAULT -> R.layout.widget
            WidgetStyle.SHADOW -> R.layout.widget_shadow
            WidgetStyle.SOLID -> R.layout.widget_solid
        }
    }

    private inline fun updateViews(
        context: Context,
        prefs: WidgetPreferences,
        partialUpdate: Boolean,
        update: (RemoteViews, Int) -> Unit
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, PimiWidget::class.java)
        )
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(
                context.packageName,
                getWidgetLayout(prefs.widgetStyle)
            )
            update(views, appWidgetId)
            if (partialUpdate) appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
            else appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
