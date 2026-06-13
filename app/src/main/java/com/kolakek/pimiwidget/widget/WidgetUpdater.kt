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
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.DataKeys
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.weather.WeatherData
import com.kolakek.pimiwidget.weather.WeatherService
import kotlinx.coroutines.runBlocking
import java.util.Date
import java.util.Locale

internal object WidgetUpdater {

    internal fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val weatherData = runBlocking {
            WeatherService.getWeatherData(context, DataKeys.WEATHER_DATA_KEY)
        }
        val prefs = PreferencesHelper.getWidgetPreferences(context)

        for (appWidgetId in appWidgetIds) {

            val views = RemoteViews(
                context.packageName,
                getWidgetLayout(prefs.textStyle)
            )
            updateBaseWidget(context, views, appWidgetId)
            val isWeatherVisible = updateWeather(context, views, prefs, weatherData)
            updateAuxDisplay(context, views, prefs, isWeatherVisible)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
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
        appWidgetId: Int
    ) {
        val pattern = DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            context.getString(R.string.widget_date_format)
        )
        views.setCharSequence(R.id.widget_text_clock, "setFormat12Hour", pattern)
        views.setCharSequence(R.id.widget_text_clock, "setFormat24Hour", pattern)

        WidgetIntent.categoryIntent(
            context,
            Intent.CATEGORY_APP_CALENDAR,
            appWidgetId
        )?.let {
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
    }

    private fun updateWeather(
        context: Context,
        views: RemoteViews,
        prefs: WidgetPreferences,
        weatherData: WeatherData?
    ): Boolean {
        views.setViewVisibility(R.id.widget_temp, View.INVISIBLE)

        if (!prefs.showWeather || weatherData == null) return false

        val strIcons = WeatherRenderer.getWidgetWeatherStrAndIcons(
            context,
            weatherData,
            prefs
        ) ?: return false

        views.apply {
            setTextViewText(R.id.widget_temp, strIcons.text)
            setTextViewCompoundDrawables(R.id.widget_temp, strIcons.iconId1, 0, strIcons.iconId2, 0)
            setViewVisibility(R.id.widget_temp, View.VISIBLE)
        }
        return true
    }

    private fun updateAuxDisplay(
        context: Context,
        views: RemoteViews,
        prefs: WidgetPreferences,
        isWeatherVisible: Boolean
    ) {
        views.setViewVisibility(R.id.widget_aux, View.INVISIBLE)

        if (!isWeatherVisible) return

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
        views.setViewVisibility(R.id.widget_aux, View.VISIBLE)

        return
    }
}
