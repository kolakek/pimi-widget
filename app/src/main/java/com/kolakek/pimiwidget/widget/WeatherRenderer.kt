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
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.resources.WarningIcon
import com.kolakek.pimiwidget.resources.WarningString
import com.kolakek.pimiwidget.resources.WeatherIcon
import com.kolakek.pimiwidget.resources.WeatherString
import com.kolakek.pimiwidget.settings.TempUnit
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.weather.WarningCode
import com.kolakek.pimiwidget.weather.WeatherData
import java.time.Instant
import java.time.ZoneId

object WeatherRenderer {

    fun getCurrentWeatherIconString(
        context: Context,
        weather: WeatherData,
        nowTimeMillis: Long,
        prefs: WidgetPreferences
    ): IconString? {
        val timeIndex = getNextTimeIndex(weather.hourlyTimeMillis, nowTimeMillis) ?: return null

        val weatherCode = weather.hourlyWeatherCode.getOrNull(timeIndex) ?: return null
        val tempCelsius = weather.hourlyTempCelsius.getOrNull(timeIndex) ?: return null
        val isDay = weather.hourlyIsDay.getOrNull(timeIndex) ?: return null

        val temperatureStr = getTemperatureString(context, tempCelsius, prefs.tempUnit)

        val weatherIconId = WeatherIcon.getWeatherIconId(
            weatherCode,
            isDay,
            prefs.iconStyle,
            prefs.iconColor
        )
        return IconString(temperatureStr, weatherIconId)
    }

    fun getWarningIconString(
        context: Context,
        nowTimeMillis: Long,
        weather: WeatherData,
        prefs: WidgetPreferences
    ): IconString? {
        val nextIndex = getNextTimeIndex(weather.hourlyTimeMillis, nowTimeMillis) ?: return null
        val warningCode = weather.hourlyWarningCode.getOrNull(nextIndex) ?: return null

        if (warningCode == WarningCode.NO_WARNING)
            return null

        return IconString(
            context.getString(WarningString.getWarningStrId(warningCode)),
            WarningIcon.getWarningIconId(warningCode.level, prefs.textColor, prefs.widgetStyle)
        )
    }

    fun getForecastString(
        context: Context,
        nowTimeMillis: Long,
        weather: WeatherData,
        prefs: WidgetPreferences
    ): String? {

        val zone = ZoneId.systemDefault()
        val zoned = Instant.ofEpochMilli(nowTimeMillis).atZone(zone)
        val date = zoned.toLocalDate()
        val hour = zoned.hour

        val isToday = when (hour) {
            in FORECAST_TODAY_HOUR_ON..<FORECAST_TODAY_HOUR_OFF -> true
            in FORECAST_TOMORROW_HOUR_ON..<FORECAST_TOMORROW_HOUR_OFF -> false

            else -> return null
        }
        val targetDate = if (isToday) date else date.plusDays(1)
        val idx = weather.dailyTimeMillis.indexOfFirst {
            Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == targetDate
        }
        if (idx == -1) {
            return null
        }
        val weatherCode = weather.dailyWeatherCode.getOrNull(idx) ?: return null
        val tempCelsiusMin = weather.dailyTempMinCelsius.getOrNull(idx) ?: return null
        val tempCelsiusMax = weather.dailyTempMaxCelsius.getOrNull(idx) ?: return null

        val minTempStr = getTemperatureString(context, tempCelsiusMin, prefs.tempUnit, false)
        val maxTempStr = getTemperatureString(context, tempCelsiusMax, prefs.tempUnit, false)

        val weatherStr = context.getString(
            WeatherString.getShortWeatherStrId(weatherCode, isDay = true)
        )
        val dayStr = context.getString(
            if (isToday) R.string.widget_today else R.string.widget_tomorrow
        )
        return "$dayStr $maxTempStr / $minTempStr · $weatherStr"
    }

    private fun getTemperatureString(
        context: Context,
        tempCelsius: Double,
        tempUnit: TempUnit,
        fullUnit: Boolean = true
    ): String {
        val isFahrenheit = (tempUnit == TempUnit.FAHRENHEIT)
        val tempValue = if (isFahrenheit) tempCelsius * 1.8 + 32 else tempCelsius
        val unit = if (fullUnit) {
            if (isFahrenheit) {
                context.getString(R.string.fahrenheit)
            } else {
                context.getString(R.string.celsius)
            }
        } else {
            context.getString(R.string.degree)
        }
        return "${(tempValue + 0.5).toInt()}$unit"
    }

    private fun getNextTimeIndex(
        timeMillis: List<Long>,
        nowTimeMillis: Long
    ): Int? {
        val idx = timeMillis.indexOfFirst { it > nowTimeMillis }

        if (idx == -1) {
            return null
        }
        return idx
    }
}
