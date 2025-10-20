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

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.PimiData
import com.kolakek.pimiwidget.data.WeatherData
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    Timber.d("updateAppWidget(): Begin Function.")

    val views = getRemoteViews(context)

    pendingCategoryIntent(context, Intent.CATEGORY_APP_CALENDAR)?.let{
        views.setOnClickPendingIntent(R.id.widget_text_clock, it)
    }

    val pendingIntent = pendingCategoryIntent(context, Intent.CATEGORY_APP_WEATHER)
        ?: pendingAltWeatherAppIntent(context)

    pendingIntent?.let{views.setOnClickPendingIntent(R.id.widget_temp, it)}

    appWidgetManager.updateAppWidget(appWidgetId, views)

    updateAppWidgetLocale(context, appWidgetManager, appWidgetId)
    updateAppWidgetWeather(context, appWidgetManager, appWidgetId)
}

internal fun updateAppWidgetWeather(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    Timber.d("updateAppWidgetWeather(): Begin Function.")

    var weatherVisibility = View.INVISIBLE

    val views = getRemoteViews(context)

    if (getWeatherPreference(context)) {

        Timber.d("updateAppWidgetWeather(): Checkpoint 1.")

        val weather = PimiData.weather
        val location = PimiData.location

        if (weather != null && location != null) {

            Timber.d("updateAppWidgetWeather(): Checkpoint 2.")

            val timeMillis = System.currentTimeMillis()

            val weatherAge = timeMillis - weather.timeMillis
            val locationAge = timeMillis - location.timeMillis

            if (weatherAge < MAX_AGE_MILLIS && locationAge < MAX_AGE_MILLIS) {

                Timber.d("updateAppWidgetWeather(): Checkpoint 3.")

                views.setTextViewText(
                    R.id.widget_temp,
                    getWeatherStr(context, weather)
                )
                views.setTextViewCompoundDrawables(
                    R.id.widget_temp,
                    mapWeatherId(
                        weather.weatherCode,
                        weather.isDay,
                        getIconColorPreference(context) == KEY_COLOR_LIGHT
                    ),
                    0,
                    0,
                    0
                )
                weatherVisibility = View.VISIBLE
            }
        }
    }

    views.setViewVisibility(R.id.widget_temp, weatherVisibility)

    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
}

internal fun updateAppWidgetLocale(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    Timber.d("updateAppWidgetLocale(): Begin Function.")

    val views = getRemoteViews(context)

    val pattern = DateFormat.getBestDateTimePattern(
        Locale.getDefault(),
        context.getString(R.string.widget_date_format)
    )
    val datePattern = SimpleDateFormat(pattern, Locale.getDefault()).toLocalizedPattern()

    views.setCharSequence(R.id.widget_text_clock, "setFormat12Hour", datePattern)
    views.setCharSequence(R.id.widget_text_clock, "setFormat24Hour", datePattern)

    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
}

internal fun updateAppWidgetLoop(
    context: Context, func: (Context, AppWidgetManager, Int) -> Unit
) {
    val manager = AppWidgetManager.getInstance(context)
    manager.getAppWidgetIds(ComponentName(context, PimiWidget::class.java))
        .forEach { appWidgetId ->
            func(context, manager, appWidgetId)
        }
}

private fun pendingCategoryIntent(
    context: Context,
    category: String
): PendingIntent? {

    val intent = Intent(Intent.ACTION_MAIN).addCategory(category)

    if (intent.resolveActivity(context.packageManager) == null) {
        val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)
        resolveInfos.firstOrNull()?.activityInfo?.let {
            intent.setComponent(ComponentName(it.packageName, it.name))
        }
    }

    return intent.resolveActivity(context.packageManager)?.let {
        PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}

private fun pendingAltWeatherAppIntent(
    context: Context,
): PendingIntent? {

    return context.packageManager.getLaunchIntentForPackage(ALT_WEATHER_APP)?.let{
        PendingIntent.getActivity(
            context,
            0,
            it,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}

private fun getRemoteViews(context: Context): RemoteViews {

    return if (getTextColorPreference(context) == KEY_COLOR_LIGHT) {
        RemoteViews(context.packageName, R.layout.pimi_widget_light)
    } else {
        RemoteViews(context.packageName, R.layout.pimi_widget_dark)
    }
}

private fun getWeatherStr(context: Context, weather: WeatherData): String {
    return if (getTempPreference(context) == KEY_FAHRENHEIT) {
        "${weather.tempF}${context.getString(R.string.fahrenheit)}"
    } else {
        "${weather.tempC}${context.getString(R.string.celsius)}"
    }
}

private fun mapWeatherId(code: Int?, isDay: Int?, lightColor: Boolean): Int {

    return when (code?.let { if (lightColor) it + 1000 else it }) {

        0 -> if (isDay == 1) R.drawable.wc_0d else R.drawable.wc_0n
        1 -> if (isDay == 1) R.drawable.wc_1d else R.drawable.wc_1n
        2 -> if (isDay == 1) R.drawable.wc_2d else R.drawable.wc_2n
        3 -> R.drawable.wc_3
        45 -> R.drawable.wc_45
        48 -> R.drawable.wc_45
        51 -> R.drawable.wc_61
        53 -> R.drawable.wc_63
        55 -> R.drawable.wc_65
        61 -> R.drawable.wc_61
        63 -> R.drawable.wc_63
        65 -> R.drawable.wc_65
        66 -> R.drawable.wc_67
        67 -> R.drawable.wc_67
        71 -> R.drawable.wc_71
        73 -> R.drawable.wc_73
        75 -> R.drawable.wc_75
        77 -> R.drawable.wc_77
        80 -> if (isDay == 1) R.drawable.wc_80d else R.drawable.wc_80n
        81 -> if (isDay == 1) R.drawable.wc_81d else R.drawable.wc_81n
        82 -> if (isDay == 1) R.drawable.wc_82d else R.drawable.wc_82n
        85 -> if (isDay == 1) R.drawable.wc_85d else R.drawable.wc_85n
        86 -> if (isDay == 1) R.drawable.wc_86d else R.drawable.wc_86n
        95 -> if (isDay == 1) R.drawable.wc_95d else R.drawable.wc_95n
        96 -> if (isDay == 1) R.drawable.wc_96d else R.drawable.wc_96n
        99 -> R.drawable.wc_99

        1000 -> if (isDay == 1) R.drawable.wb_0d else R.drawable.wb_0n
        1001 -> if (isDay == 1) R.drawable.wb_1d else R.drawable.wb_1n
        1002 -> if (isDay == 1) R.drawable.wb_2d else R.drawable.wb_2n
        1003 -> R.drawable.wb_3
        1045 -> R.drawable.wb_45
        1048 -> R.drawable.wb_45
        1051 -> R.drawable.wb_61
        1053 -> R.drawable.wb_63
        1055 -> R.drawable.wb_65
        1061 -> R.drawable.wb_61
        1063 -> R.drawable.wb_63
        1065 -> R.drawable.wb_65
        1066 -> R.drawable.wb_67
        1067 -> R.drawable.wb_67
        1071 -> R.drawable.wb_71
        1073 -> R.drawable.wb_73
        1075 -> R.drawable.wb_75
        1077 -> R.drawable.wb_77
        1080 -> if (isDay == 1) R.drawable.wb_80d else R.drawable.wb_80n
        1081 -> if (isDay == 1) R.drawable.wb_81d else R.drawable.wb_81n
        1082 -> if (isDay == 1) R.drawable.wb_82d else R.drawable.wb_82n
        1085 -> if (isDay == 1) R.drawable.wb_85d else R.drawable.wb_85n
        1086 -> if (isDay == 1) R.drawable.wb_86d else R.drawable.wb_86n
        1095 -> if (isDay == 1) R.drawable.wb_95d else R.drawable.wb_95n
        1096 -> if (isDay == 1) R.drawable.wb_96d else R.drawable.wb_96n
        1099 -> R.drawable.wb_99

        else -> R.drawable.wc_nan
    }
}
