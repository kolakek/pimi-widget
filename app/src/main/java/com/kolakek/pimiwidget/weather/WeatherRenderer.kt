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

package com.kolakek.pimiwidget.weather

import android.content.Context
import android.text.format.DateFormat
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.resources.WarningIcon
import com.kolakek.pimiwidget.resources.WarningString
import com.kolakek.pimiwidget.resources.WeatherIcon
import com.kolakek.pimiwidget.resources.WeatherString
import com.kolakek.pimiwidget.settings.AuxDisplay
import com.kolakek.pimiwidget.settings.IconColor
import com.kolakek.pimiwidget.settings.IconStyle
import com.kolakek.pimiwidget.settings.TempUnit
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.widget.FORECAST_TODAY_HOUR_OFF
import com.kolakek.pimiwidget.widget.FORECAST_TODAY_HOUR_ON
import com.kolakek.pimiwidget.widget.FORECAST_TOMORROW_HOUR_OFF
import com.kolakek.pimiwidget.widget.FORECAST_TOMORROW_HOUR_ON
import com.kolakek.pimiwidget.utility.LabeledIcon
import java.time.Instant
import java.time.ZoneId
import java.util.Date

object WeatherRenderer {

    fun currentWeather(
        context: Context,
        weather: WeatherData,
        iconStyle: IconStyle,
        iconColor: IconColor,
        tempUnit: TempUnit,
        fullUnit: Boolean = true
    ): LabeledIcon? {
        val weatherCode = weather.currentWeatherCode() ?: return null
        val tempCelsius = weather.currentTempCelsius() ?: return null
        val isDay = weather.currentIsDay() ?: return null

        val temperatureStr = temperatureString(context, tempCelsius, tempUnit, fullUnit)

        val weatherIconId = WeatherIcon.getWeatherIconId(
            weatherCode,
            isDay,
            iconStyle,
            iconColor
        )
        return LabeledIcon(temperatureStr, weatherIconId)
    }

    fun currentWarning(
        context: Context,
        weather: WeatherData,
        prefs: WidgetPreferences
    ): LabeledIcon? {
        val warningCode = weather.currentWarningCode() ?: return null

        if (warningCode == WarningCode.NO_WARNING)
            return null

        return LabeledIcon(
            context.getString(WarningString.getWarningStrId(warningCode)),
            WarningIcon.getWarningIconId(warningCode.level, prefs.textColor, prefs.widgetStyle)
        )
    }

    fun forecastString(
        context: Context,
        nowTimeMillis: Long,
        weather: WeatherData,
        prefs: WidgetPreferences
    ): String? {

        val zone = ZoneId.systemDefault()
        val zoned = Instant.ofEpochMilli(nowTimeMillis).atZone(zone)
        val date = zoned.toLocalDate()
        val hour = zoned.hour

        val useToday = when (hour) {
            in FORECAST_TODAY_HOUR_ON..<FORECAST_TODAY_HOUR_OFF -> true
            in FORECAST_TOMORROW_HOUR_ON..<FORECAST_TOMORROW_HOUR_OFF -> false

            else -> return null
        }
        val targetDate = if (useToday) date else date.plusDays(1)
        val idx = weather.dailyTimeMillis.indexOfFirst {
            Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == targetDate
        }
        if (idx == -1) {
            return null
        }
        val weatherCode = weather.dailyWeatherCode.getOrNull(idx) ?: return null
        val tempCelsiusMin = weather.dailyTempMinCelsius.getOrNull(idx) ?: return null
        val tempCelsiusMax = weather.dailyTempMaxCelsius.getOrNull(idx) ?: return null

        val minTempStr = temperatureString(context, tempCelsiusMin, prefs.tempUnit, false)
        val maxTempStr = temperatureString(context, tempCelsiusMax, prefs.tempUnit, false)

        val weatherStr = context.getString(
            WeatherString.shortStringId(weatherCode, isDay = true)
        )
        val dayStr = context.getString(
            if (useToday) R.string.widget_today else R.string.widget_tomorrow
        )
        return "$dayStr $maxTempStr / $minTempStr · $weatherStr"
    }

    fun currentFeelsLikeStr(
        context: Context,
        weather: WeatherData,
        tempUnit: TempUnit,
        fullUnit: Boolean = true
    ): String? {
        val currentApparentTemp = weather.currentApparentTempCelsius() ?: return null
        return context.getString(R.string.app_text_feels_like) + " " +
                temperatureString(context, currentApparentTemp, tempUnit, fullUnit)
    }

    fun dailyHighLowTempStr(
        context: Context,
        weather: WeatherData,
        tempUnit: TempUnit,
        fullUnit: Boolean = true
    ): String? {
        val minTemp = weather.todayMinTempCelsius() ?: return null
        val maxTemp = weather.todayMaxTempCelsius() ?: return null

        val minTempStr = temperatureString(context, minTemp, tempUnit, fullUnit)
        val maxTempStr = temperatureString(context, maxTemp, tempUnit, fullUnit)

        return context.getString(R.string.app_text_high) + " " + maxTempStr + " · " +
                context.getString(R.string.app_text_low) + " " + minTempStr
    }

    fun auxString(
        context: Context,
        nowTimeMillis: Long,
        prefs: WidgetPreferences
    ): String? {
        return when (prefs.auxDisplay) {

            AuxDisplay.NOTHING -> null

            AuxDisplay.UPDATE_TIME -> {
                val str = DateFormat.getTimeFormat(context).format(Date(nowTimeMillis))
                context.getString(R.string.widget_updated_at) + " $str"
            }
        }
    }

    fun temperatureString(
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
}
