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
import com.kolakek.pimiwidget.data.SettingsRepository
import com.kolakek.pimiwidget.data.WidgetStyle
import com.kolakek.pimiwidget.weather.WeatherData
import kotlinx.coroutines.runBlocking

object WidgetUpdater {

    fun updateWidgets(context: Context) {
        val settings = SettingsRepository.loadWidgetSettings(context)

        val weatherData = if (settings.showWeather) {
            runBlocking { DataRepository.loadWeatherData(context) }
        } else null

        val birthdayData = if (settings.showBirthday) {
            runBlocking { DataRepository.loadBirthdayData(context) }
        } else null

        updateViews(context, settings, partialUpdate = false) { views, appWidgetId ->
            CoreUpdater.updateViews(context, views, appWidgetId, settings)
            AlarmUpdater.updateViews(context, views, settings)
            WeatherUpdater.updateViews(context, views, settings, weatherData)
            val birthdayStatus = BirthdayUpdater.updateViews(context, views, settings, birthdayData)
            VisibilityUpdater.updateEventViews(views, birthdayStatus)
        }
    }

    fun refreshWidgets(
        context: Context,
        settings: WidgetSettings,
        weatherData: WeatherData?,
        birthdayData: BirthdayData?
    ): WeatherUpdateStatus {
        var lastWeatherStatus = WeatherUpdateStatus.NO_ACTION_NEEDED

        updateViews(context, settings, partialUpdate = true) { views, _ ->
            AlarmUpdater.updateViews(context, views, settings)
            lastWeatherStatus = WeatherUpdater.updateViews(context, views, settings, weatherData)
            val birthdayStatus = BirthdayUpdater.updateViews(context, views, settings, birthdayData)
            VisibilityUpdater.updateEventViews(views, birthdayStatus)
        }
        return lastWeatherStatus
    }

    fun refreshWeather(
        context: Context,
        settings: WidgetSettings,
        weatherData: WeatherData?
    ) {
        updateViews(context, settings, partialUpdate = true) { views, _ ->
            WeatherUpdater.updateViews(context, views, settings, weatherData)
        }
    }

    fun refreshAlarms(context: Context) {
        val settings = SettingsRepository.loadWidgetSettings(context)
        updateViews(context, settings, partialUpdate = true) { views, _ ->
            AlarmUpdater.updateViews(context, views, settings)
        }
    }

    fun refreshBirthdays(context: Context) {
        val settings = SettingsRepository.loadWidgetSettings(context)
        val birthdayData = if (settings.showBirthday) {
            runBlocking { DataRepository.loadBirthdayData(context) }
        } else null

        refreshBirthdays(context, settings, birthdayData)
    }

    fun refreshBirthdays(
        context: Context,
        settings: WidgetSettings,
        birthdayData: BirthdayData?
    ) {
        updateViews(context, settings, partialUpdate = true) { views, _ ->
            val birthdayStatus = BirthdayUpdater.updateViews(context, views, settings, birthdayData)
            VisibilityUpdater.updateEventViews(views, birthdayStatus)
        }
    }

    private fun getWidgetLayout(widgetStyle: WidgetStyle): Int {
        return when (widgetStyle) {
            WidgetStyle.SOLID -> R.layout.widget_solid
            WidgetStyle.CLASSIC -> R.layout.widget_classic
            WidgetStyle.CLASSIC_SHADOW -> R.layout.widget_classic_shadow
            WidgetStyle.MODERN -> R.layout.widget_modern
            WidgetStyle.MODERN_SHADOW -> R.layout.widget_modern_shadow
        }
    }

    private inline fun updateViews(
        context: Context,
        settings: WidgetSettings,
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
                getWidgetLayout(settings.widgetStyle)
            )
            update(views, appWidgetId)
            if (partialUpdate) appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
            else appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
