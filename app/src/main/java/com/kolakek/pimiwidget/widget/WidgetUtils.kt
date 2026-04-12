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
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.PimiData
import com.kolakek.pimiwidget.data.WeatherData
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
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
    Timber.d("updateAppWidget(): Begin Function, update mode $updateMode.")

    val showWeather = getWeatherPreference(context)
    val showForecast = getDailyForecastPreference(context)
    val tempUnit = getTempPreference(context)
    val iconStyle = getIconStylePreference(context)
    val textColor = getTextColorPreference(context)

    val lightText = useLightText(context, textColor)
    val views = getRemoteViews(context, iconStyle, lightText)

    if (updateMode == WidgetUpdateMode.APP_WIDGET) {
        updateAppWidgetFull(context, views, appWidgetManager, appWidgetId)
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
            PimiData.weather,
            showWeather,
            showForecast,
            tempUnit,
            iconStyle,
            lightText
        )
    }
}

private fun updateAppWidgetFull(
    context: Context,
    views: RemoteViews,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
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

private fun updateAppWidgetDate(
    context: Context,
    views: RemoteViews,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    Timber.d("updateAppWidgetDate(): Begin Function.")

    val pattern = DateFormat.getBestDateTimePattern(
        Locale.getDefault(),
        context.getString(R.string.widget_date_format)
    )

    Timber.d("updateAppWidgetDate(): Set pattern $pattern.")

    views.setCharSequence(R.id.widget_text_clock, "setFormat12Hour", pattern)
    views.setCharSequence(R.id.widget_text_clock, "setFormat24Hour", pattern)

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

            Timber.d("updateAppWidgetWeather(): Refreshing weather display.")

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
            if (displayStr != null && iconId != null) {
                views.setTextViewText(R.id.widget_temp, displayStr)
                views.setTextViewCompoundDrawables(R.id.widget_temp, iconId, 0, 0, 0)
                views.setViewVisibility(R.id.widget_temp, View.VISIBLE)
            } else {
                Timber.w("updateAppWidgetWeather(): Null displayStr or iconId.")
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

    if (idx == -1) {
        Timber.w("getCurrentWeatherStrAndIcon(): No data available for the next hour.")
        return null to null
    }
    val str = getTemperatureStr(
        context,
        weather.hourlyTempCelsius.getOrNull(idx),
        tempUnit
    )
    val id = mapWeatherId(
        weather.hourlyWeatherCode.getOrNull(idx),
        weather.hourlyIsDay.getOrNull(idx),
        iconStyle,
        lightText
    )
    if (str == null || id == null) {
        Timber.w("getCurrentWeatherStrAndIcon(): Unexpected null return.")
    }
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

     val today = when (hour) {
        in FORECAST_TODAY_HOUR_ON..<FORECAST_TODAY_HOUR_OFF -> true
        in FORECAST_TOMORROW_HOUR_ON..<FORECAST_TOMORROW_HOUR_OFF -> false
        else -> return null
    }
    val targetDate = if (today) date else date.plusDays(1)
    val idx = weather.dailyTimeMillis.indexOfFirst {
        Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == targetDate
    }
    Timber.d("getForecastStr(): Target date $targetDate at index $idx.")

    if (idx == -1) {
        Timber.w("getForecastStr(): No forecast data available.")
        return null
    }
    val minTempStr = getTemperatureStr(
        context,
        weather.dailyTempMinCelsius.getOrNull(idx),
        tempUnit,
        false
    )
    val maxTempStr = getTemperatureStr(
        context,
        weather.dailyTempMaxCelsius.getOrNull(idx),
        tempUnit,
        false
    )
    val codeStrId = getWeatherCodeId(
        weather.dailyWeatherCode.getOrNull(idx)
    )
    val dayStrId = if (today) R.string.today else R.string.tomorrow

    if (minTempStr == null || maxTempStr == null || codeStrId == null) {
        Timber.w("getForecastStr(): Unexpected null return.")
        return null
    }

    return context.getString(
        R.string.forecast_line,
        context.getString(dayStrId),
        context.getString(codeStrId),
        maxTempStr,
        minTempStr
    )
}

private fun getTemperatureStr(
    context: Context,
    tempCelsius: Double?,
    tempUnit: String,
    fullUnit: Boolean = true
): String? {
    tempCelsius ?: return null

    val useFahrenheit = (tempUnit == KEY_FAHRENHEIT)
    val temp = (if (useFahrenheit) tempCelsius * 1.8 + 32.5 else tempCelsius + 0.5).toInt()
    val unit = when {
        fullUnit && useFahrenheit -> context.getString(R.string.fahrenheit)
        fullUnit -> context.getString(R.string.celsius)
        else -> context.getString(R.string.degree)
    }
    return "$temp$unit"
}

private fun getWeatherCodeId(code: Int?): Int? {
    return when (code) {

        0 -> R.string.w0
        1 -> R.string.w1
        2 -> R.string.w2
        3 -> R.string.w3
        45, 48 -> R.string.w45
        51 -> R.string.w51
        53 -> R.string.w53
        55 -> R.string.w55
        56, 57 -> R.string.w56
        61 -> R.string.w61
        63 -> R.string.w63
        65 -> R.string.w65
        66, 67 -> R.string.w66
        71 -> R.string.w71
        73 -> R.string.w73
        75 -> R.string.w75
        77 -> R.string.w77
        80 -> R.string.w80
        81 -> R.string.w81
        82 -> R.string.w82
        85 -> R.string.w85
        86 -> R.string.w86
        95, 96, 99 -> R.string.w95

        else -> {
            Timber.w("getWeatherCodeId(): Invalid weather code.")
            null
        }
    }
}

private fun mapWeatherId(
    code: Int?,
    isDay: Int?,
    style: String,
    darkColor: Boolean
): Int? {
    return when {

        code == null || isDay == null -> {
            Timber.w("mapWeatherId(): Invalid weather code or isDay value.")
            null
        }

        style == KEY_ICON_STYLE_FILLED && darkColor -> iconIdFilledDark(code, isDay)
        style == KEY_ICON_STYLE_FILLED -> iconIdFilled(code, isDay)
        style == KEY_ICON_STYLE_OUTLINED && darkColor -> iconIdOutlinedDark(code, isDay)
        style == KEY_ICON_STYLE_OUTLINED -> iconIdOutlined(code, isDay)

        else -> {
            Timber.w("mapWeatherId(): Unexpected null return.")
            null
        }
    }
}

private fun iconIdFilled(code: Int, isDay: Int): Int? {
    return when (code) {

        0 -> if (isDay == 1) R.drawable.wb_0d else R.drawable.wb_0n
        1 -> if (isDay == 1) R.drawable.wb_1d else R.drawable.wb_1n
        2 -> if (isDay == 1) R.drawable.wb_2d else R.drawable.wb_2n
        3 -> R.drawable.wb_3
        45, 48 -> R.drawable.wb_45
        51, 61 -> R.drawable.wb_61
        53, 63 -> R.drawable.wb_63
        55, 65 -> R.drawable.wb_65
        56, 57, 66, 67 -> R.drawable.wb_67
        71 -> R.drawable.wb_71
        73 -> R.drawable.wb_73
        75 -> R.drawable.wb_75
        77 -> R.drawable.wb_77
        80 -> if (isDay == 1) R.drawable.wb_80d else R.drawable.wb_80n
        81 -> if (isDay == 1) R.drawable.wb_81d else R.drawable.wb_81n
        82 -> if (isDay == 1) R.drawable.wb_82d else R.drawable.wb_82n
        85 -> if (isDay == 1) R.drawable.wb_85d else R.drawable.wb_85n
        86 -> if (isDay == 1) R.drawable.wb_86d else R.drawable.wb_86n
        95 -> if (isDay == 1) R.drawable.wb_95d else R.drawable.wb_95n
        96 -> if (isDay == 1) R.drawable.wb_96d else R.drawable.wb_96n
        99 -> R.drawable.wb_99

        else -> {
            Timber.w("iconIdFilled(): Invalid weather code.")
            null
        }
    }
}

private fun iconIdFilledDark(code: Int, isDay: Int): Int? {
    return when (code) {

        0 -> if (isDay == 1) R.drawable.wc_0d else R.drawable.wc_0n
        1 -> if (isDay == 1) R.drawable.wc_1d else R.drawable.wc_1n
        2 -> if (isDay == 1) R.drawable.wc_2d else R.drawable.wc_2n
        3 -> R.drawable.wc_3
        45, 48 -> R.drawable.wc_45
        51, 61 -> R.drawable.wc_61
        53, 63 -> R.drawable.wc_63
        55, 65 -> R.drawable.wc_65
        56, 57, 66, 67 -> R.drawable.wc_67
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

        else -> {
            Timber.w("iconIdFilledDark(): Invalid weather code.")
            null
        }
    }
}

private fun iconIdOutlined(code: Int, isDay: Int): Int? {
    return when (code) {

        0 -> if (isDay == 1) R.drawable.ub_0d else R.drawable.ub_0n
        1 -> if (isDay == 1) R.drawable.ub_1d else R.drawable.ub_1n
        2 -> if (isDay == 1) R.drawable.ub_2d else R.drawable.ub_2n
        3 -> R.drawable.ub_3
        45, 48 -> R.drawable.ub_45
        51, 61 -> R.drawable.ub_61
        53, 63 -> R.drawable.ub_63
        55, 65 -> R.drawable.ub_65
        56, 57, 66, 67 -> R.drawable.ub_67
        71 -> R.drawable.ub_71
        73 -> R.drawable.ub_73
        75 -> R.drawable.ub_75
        77 -> R.drawable.ub_77
        80 -> if (isDay == 1) R.drawable.ub_80d else R.drawable.ub_80n
        81 -> if (isDay == 1) R.drawable.ub_81d else R.drawable.ub_81n
        82 -> if (isDay == 1) R.drawable.ub_82d else R.drawable.ub_82n
        85 -> if (isDay == 1) R.drawable.ub_85d else R.drawable.ub_85n
        86 -> if (isDay == 1) R.drawable.ub_86d else R.drawable.ub_86n
        95 -> if (isDay == 1) R.drawable.ub_95d else R.drawable.ub_95n
        96 -> if (isDay == 1) R.drawable.ub_96d else R.drawable.ub_96n
        99 -> R.drawable.ub_99

        else -> {
            Timber.w("iconIdOutlined(): Invalid weather code.")
            null
        }
    }
}

private fun iconIdOutlinedDark(code: Int, isDay: Int): Int? {
    return when (code) {

        0 -> if (isDay == 1) R.drawable.uc_0d else R.drawable.uc_0n
        1 -> if (isDay == 1) R.drawable.uc_1d else R.drawable.uc_1n
        2 -> if (isDay == 1) R.drawable.uc_2d else R.drawable.uc_2n
        3 -> R.drawable.uc_3
        45, 48 -> R.drawable.uc_45
        51, 61 -> R.drawable.uc_61
        53, 63 -> R.drawable.uc_63
        55, 65 -> R.drawable.uc_65
        56, 57, 66, 67 -> R.drawable.uc_67
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

        else -> {
            Timber.w("iconIdOutlinedDark(): Invalid weather code.")
            null
        }
    }
}
