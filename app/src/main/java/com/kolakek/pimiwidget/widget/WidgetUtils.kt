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
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.PimiData
import com.kolakek.pimiwidget.data.WeatherData
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun updateAppWidgetLoop(
    context: Context,
    updateMode: WidgetUpdateMode
) {
    val manager = AppWidgetManager.getInstance(context)
    manager.getAppWidgetIds(ComponentName(context, PimiWidget::class.java))
        .forEach { appWidgetId ->
            updateAppWidget(context, manager, appWidgetId, updateMode)
        }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    updateMode: WidgetUpdateMode = WidgetUpdateMode.APP_WIDGET
) {
    Timber.d("updateAppWidget(): Begin Function.")

    val showWeather = getWeatherPreference(context)
    val showForecast = getDailyForecastPreference(context)
    val tempUnit = getTempPreference(context)
    val iconStyle = getIconStylePreference(context)
    val textColor = getTextColorPreference(context)

    val lightText = useLightText(context, textColor)
    val views = getRemoteViews(context, iconStyle, lightText)

    val weatherData = PimiData.weather

    if (updateMode == WidgetUpdateMode.APP_WIDGET) {
        pendingCategoryIntent(context, Intent.CATEGORY_APP_CALENDAR, appWidgetId)?.let {
            views.setOnClickPendingIntent(R.id.widget_text_clock, it)
        }
        val pendingIntent = pendingCategoryIntent(context, Intent.CATEGORY_APP_WEATHER, appWidgetId)
            ?: pendingAltWeatherAppIntent(context, appWidgetId)

        pendingIntent?.let {
            views.setOnClickPendingIntent(R.id.widget_temp, it)
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    if (updateMode == WidgetUpdateMode.APP_WIDGET ||
        updateMode == WidgetUpdateMode.LOCALE
    ) {
        updateAppWidgetDate(context, views, appWidgetManager, appWidgetId)
    }
    if (updateMode == WidgetUpdateMode.APP_WIDGET ||
        updateMode == WidgetUpdateMode.LOCALE ||
        updateMode == WidgetUpdateMode.WEATHER
    ) {
        updateAppWidgetWeather(
            context,
            views,
            appWidgetManager,
            appWidgetId,
            weatherData,
            showWeather,
            showForecast,
            tempUnit,
            iconStyle,
            lightText
        )
    }
}

private fun updateAppWidgetDate(
    context: Context,
    views: RemoteViews,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    Timber.d("updateAppWidgetDate(): Begin Function.")

    val datePattern = context.getString(R.string.widget_date_format)
    val formatter = DateTimeFormatter.ofPattern(datePattern, Locale.getDefault())
    val formattedDate = LocalDateTime.now().format(formatter)

    views.setTextViewText(R.id.widget_text_clock, formattedDate)

    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
}

private fun updateAppWidgetWeather(
    context: Context,
    views: RemoteViews,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    weatherData: WeatherData?,
    showWeather: Boolean,
    showForecast: Boolean,
    tempUnit: String,
    iconStyle: String,
    lightText: Boolean
) {
    Timber.d("updateAppWidgetWeather(): Begin Function.")

    views.setViewVisibility(R.id.widget_temp, View.INVISIBLE)

    if (showWeather) {
        weatherData?.let { weather ->

            Timber.d("updateAppWidgetWeather(): Checkpoint 1.")

            val timeMillis = System.currentTimeMillis()
            val (str, iconId) = getCurrentWeatherStrAndIcon(
                context,
                weather,
                timeMillis,
                tempUnit,
                iconStyle,
                lightText
            )
            val displayStr = str?.run {
                if (showForecast) {
                    this + (getForecastStr(context, timeMillis, weather, tempUnit) ?: "")
                } else this
            }
            displayStr?.let {
                views.setTextViewText(R.id.widget_temp, it)
                iconId?.let { id ->
                    views.setTextViewCompoundDrawables(R.id.widget_temp, id, 0, 0, 0)
                }
                views.setViewVisibility(R.id.widget_temp, View.VISIBLE)
            }
        }
    }

    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
}

private fun pendingCategoryIntent(
    context: Context,
    category: String,
    requestCode: Int
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
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}

private fun pendingAltWeatherAppIntent(
    context: Context,
    requestCode: Int
): PendingIntent? {

    return context.packageManager.getLaunchIntentForPackage(ALT_WEATHER_APP)?.let {
        PendingIntent.getActivity(
            context,
            requestCode,
            it,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}

private fun getRemoteViews(context: Context, iconStyle: String, lightText: Boolean) =
    RemoteViews(
        context.packageName,
        when {
            lightText && (iconStyle == KEY_ICON_STYLE_OUTLINED) ->
                R.layout.pimi_widget_light

            lightText ->
                R.layout.pimi_widget_light_shadow

            else ->
                R.layout.pimi_widget_dark
        }
    )

private fun useLightText(context: Context, textColor: String): Boolean =
    when (textColor) {
        KEY_COLOR_AUTO -> WallpaperManager.getInstance(context)
            .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            ?.colorHints
            ?.let { it and WallpaperColors.HINT_SUPPORTS_DARK_TEXT == 0 } ?: true

        KEY_COLOR_LIGHT -> true
        else -> false
    }

private fun getCurrentWeatherStrAndIcon(
    context: Context,
    weather: WeatherData,
    timeMillis: Long,
    tempUnit: String,
    iconStyle: String,
    lightText: Boolean
): Pair<String?, Int?> {
    val idx = weather.hourlyTimeMillis.indexOfFirst { it > timeMillis }

    if (idx == -1 ||
        idx >= weather.hourlyTempCelsius.size ||
        idx >= weather.hourlyWeatherCode.size ||
        idx >= weather.hourlyIsDay.size
    ) return null to null

    val str = getTemperatureStr(
        context,
        weather.hourlyTempCelsius[idx],
        tempUnit
    )

    val id = mapWeatherId(
        weather.hourlyWeatherCode[idx],
        weather.hourlyIsDay[idx],
        iconStyle,
        !lightText
    )

    return str to id
}

private fun getForecastStr(
    context: Context,
    timeMillis: Long,
    weather: WeatherData,
    tempUnit: String
): String? {

    val zone = ZoneId.systemDefault()
    val zoned = Instant.ofEpochMilli(timeMillis).atZone(zone)
    val date = zoned.toLocalDate()
    val hour = zoned.hour

    val targetDate = when {
        hour <= DAILY_FORECAST_BEFORE_HOUR -> date
        hour >= DAILY_FORECAST_AFTER_HOUR -> date.plusDays(1)
        else -> return null
    }

    val idx = weather.dailyTimeMillis.indexOfFirst {
        Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == targetDate
    }

    if (idx == -1 ||
        idx >= weather.dailyTempMinCelsius.size ||
        idx >= weather.dailyTempMaxCelsius.size ||
        idx >= weather.dailyWeatherCode.size
    ) return null

    val minStr = getTemperatureStr(context, weather.dailyTempMinCelsius[idx], tempUnit, false)
    val maxStr = getTemperatureStr(context, weather.dailyTempMaxCelsius[idx], tempUnit, false)
    val codeStr = getWeatherCodeStr(context, weather.dailyWeatherCode[idx])

    val dayStr = if (hour < DAILY_FORECAST_BEFORE_HOUR) {
        context.getString(R.string.today)
    } else {
        context.getString(R.string.tomorrow)
    }

    return " · $dayStr $maxStr / $minStr · $codeStr"
}

private fun getTemperatureStr(
    context: Context,
    tempC: Double,
    tempUnit: String,
    fullUnit: Boolean = true
): String {
    val useFahrenheit = (tempUnit == KEY_FAHRENHEIT)
    val temp = if (useFahrenheit) (tempC * 1.8 + 32.5).toInt() else (tempC + 0.5).toInt()
    val unit = when {
        fullUnit && useFahrenheit -> context.getString(R.string.fahrenheit)
        fullUnit -> context.getString(R.string.celsius)
        else -> context.getString(R.string.degree)
    }
    return "$temp$unit"
}

private fun getWeatherCodeStr(context: Context, code: Int): String {
    return when (code) {

        0 -> context.getString(R.string.w0)
        1 -> context.getString(R.string.w1)
        2 -> context.getString(R.string.w2)
        3 -> context.getString(R.string.w3)
        45 -> context.getString(R.string.w45)
        48 -> context.getString(R.string.w45)
        51 -> context.getString(R.string.w51)
        53 -> context.getString(R.string.w53)
        55 -> context.getString(R.string.w55)
        56 -> context.getString(R.string.w56)
        57 -> context.getString(R.string.w56)
        61 -> context.getString(R.string.w61)
        63 -> context.getString(R.string.w63)
        65 -> context.getString(R.string.w65)
        66 -> context.getString(R.string.w66)
        67 -> context.getString(R.string.w66)
        71 -> context.getString(R.string.w71)
        73 -> context.getString(R.string.w73)
        75 -> context.getString(R.string.w75)
        77 -> context.getString(R.string.w77)
        80 -> context.getString(R.string.w80)
        81 -> context.getString(R.string.w81)
        82 -> context.getString(R.string.w82)
        85 -> context.getString(R.string.w85)
        86 -> context.getString(R.string.w86)
        95 -> context.getString(R.string.w95)
        96 -> context.getString(R.string.w95)
        99 -> context.getString(R.string.w95)

        else -> ""
    }
}

private fun mapWeatherId(
    code: Int?,
    isDay: Int?,
    iconStyle: String,
    lightColor: Boolean
) =
    if (iconStyle == KEY_ICON_STYLE_OUTLINED)
        mapWeatherIdOutlined(code, isDay, lightColor)
    else
        mapWeatherIdFilled(code, isDay, lightColor)

private fun mapWeatherIdFilled(code: Int?, isDay: Int?, lightColor: Boolean): Int {
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

private fun mapWeatherIdOutlined(code: Int?, isDay: Int?, lightColor: Boolean): Int {
    return when (code?.let { if (lightColor) it + 1000 else it }) {

        0 -> if (isDay == 1) R.drawable.uc_0d else R.drawable.uc_0n
        1 -> if (isDay == 1) R.drawable.uc_1d else R.drawable.uc_1n
        2 -> if (isDay == 1) R.drawable.uc_2d else R.drawable.uc_2n
        3 -> R.drawable.uc_3
        45 -> R.drawable.uc_45
        48 -> R.drawable.uc_45
        51 -> R.drawable.uc_61
        53 -> R.drawable.uc_63
        55 -> R.drawable.uc_65
        61 -> R.drawable.uc_61
        63 -> R.drawable.uc_63
        65 -> R.drawable.uc_65
        66 -> R.drawable.uc_67
        67 -> R.drawable.uc_67
        71 -> R.drawable.uc_71
        73 -> R.drawable.uc_73
        75 -> R.drawable.uc_75
        77 -> R.drawable.uc_77
        80 -> if (isDay == 1) R.drawable.uc_80d else R.drawable.uc_80n
        81 -> if (isDay == 1) R.drawable.uc_81d else R.drawable.uc_81n
        82 -> if (isDay == 1) R.drawable.uc_82d else R.drawable.uc_82n
        85 -> if (isDay == 1) R.drawable.uc_85d else R.drawable.uc_85n
        86 -> if (isDay == 1) R.drawable.uc_86d else R.drawable.uc_86n
        95 -> if (isDay == 1) R.drawable.uc_95d else R.drawable.uc_95n
        96 -> if (isDay == 1) R.drawable.uc_96d else R.drawable.uc_96n
        99 -> R.drawable.uc_99

        1000 -> if (isDay == 1) R.drawable.ub_0d else R.drawable.ub_0n
        1001 -> if (isDay == 1) R.drawable.ub_1d else R.drawable.ub_1n
        1002 -> if (isDay == 1) R.drawable.ub_2d else R.drawable.ub_2n
        1003 -> R.drawable.ub_3
        1045 -> R.drawable.ub_45
        1048 -> R.drawable.ub_45
        1051 -> R.drawable.ub_61
        1053 -> R.drawable.ub_63
        1055 -> R.drawable.ub_65
        1061 -> R.drawable.ub_61
        1063 -> R.drawable.ub_63
        1065 -> R.drawable.ub_65
        1066 -> R.drawable.ub_67
        1067 -> R.drawable.ub_67
        1071 -> R.drawable.ub_71
        1073 -> R.drawable.ub_73
        1075 -> R.drawable.ub_75
        1077 -> R.drawable.ub_77
        1080 -> if (isDay == 1) R.drawable.ub_80d else R.drawable.ub_80n
        1081 -> if (isDay == 1) R.drawable.ub_81d else R.drawable.ub_81n
        1082 -> if (isDay == 1) R.drawable.ub_82d else R.drawable.ub_82n
        1085 -> if (isDay == 1) R.drawable.ub_85d else R.drawable.ub_85n
        1086 -> if (isDay == 1) R.drawable.ub_86d else R.drawable.ub_86n
        1095 -> if (isDay == 1) R.drawable.ub_95d else R.drawable.ub_95n
        1096 -> if (isDay == 1) R.drawable.ub_96d else R.drawable.ub_96n
        1099 -> R.drawable.ub_99

        else -> R.drawable.wc_nan
    }
}
