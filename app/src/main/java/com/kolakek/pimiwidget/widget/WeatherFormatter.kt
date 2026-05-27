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
import com.kolakek.pimiwidget.resources.IconStyle
import com.kolakek.pimiwidget.resources.WarningIcon
import com.kolakek.pimiwidget.resources.WarningString
import com.kolakek.pimiwidget.resources.WeatherIcon
import com.kolakek.pimiwidget.resources.WeatherString
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.weather.WarningLevel
import com.kolakek.pimiwidget.weather.WarningCode
import com.kolakek.pimiwidget.weather.WarningCodeMapper
import com.kolakek.pimiwidget.weather.WeatherData
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId

internal object WeatherFormatter {

    internal fun getWidgetWeatherStrAndIcons(
        context: Context,
        weather: WeatherData,
        prefs: WidgetPreferences
    ): TextWithTwoIcons? {

        val nowTimeMillis = System.currentTimeMillis()

        val currentWeatherInfo = getCurrentWeatherStrAndIcon(
            context,
            nowTimeMillis,
            weather,
            prefs.tempUnit,
            prefs.iconStyle
        ) ?: return null

        val warningStrAndIcon = if (prefs.showWeatherWarning) {
            getWarningStrAndIcon(
                context,
                nowTimeMillis,
                weather,
                prefs.textStyle
            )
        } else null

        val forecastStr = if (warningStrAndIcon == null && prefs.showDailyForecast) {
            getForecastStr(
                context,
                nowTimeMillis,
                weather,
                prefs.tempUnit
            )
        } else null

        val extraIconId = warningStrAndIcon?.iconId ?: 0

        val extraText = (warningStrAndIcon?.text ?: forecastStr)?.prependIndent(" · ").orEmpty()
        val widgetStr = currentWeatherInfo.text + extraText

        return TextWithTwoIcons(widgetStr, currentWeatherInfo.iconId, extraIconId)
    }

    private fun getCurrentWeatherStrAndIcon(
        context: Context,
        nowTimeMillis: Long,
        weather: WeatherData,
        tempUnit: TempUnit,
        iconStyle: IconStyle
    ): TextWithOneIcon? {
        val timeIndex = getNextTimeIndex(weather.minutelyTimeMillis, nowTimeMillis) ?: return null

        Timber.d("getCurrentWeatherStrAndIcon: Time index $timeIndex")

        val weatherCode = weather.minutelyWeatherCode.getOrNull(timeIndex) ?: return null
        val tempCelsius = weather.minutelyTempCelsius.getOrNull(timeIndex) ?: return null
        val isDay = weather.minutelyIsDay.getOrNull(timeIndex) ?: return null

        val temperatureStr = getTemperatureStr(context, tempCelsius, tempUnit)

        val weatherIconId = WeatherIcon.getWeatherIconId(
            weatherCode,
            isDay,
            iconStyle
        )
        return TextWithOneIcon(temperatureStr, weatherIconId)
    }

    private fun getWarningStrAndIcon(
        context: Context,
        nowTimeMillis: Long,
        weather: WeatherData,
        textStyle: TextStyle
    ): TextWithOneIcon? {
        val nextIndex = getNextTimeIndex(weather.hourlyTimeMillis, nowTimeMillis) ?: return null
        val prevIndex = getPrevTimeIndex(weather.hourlyTimeMillis, nowTimeMillis) ?: return null

        Timber.d("getWarningStrAndIcon: prevIndex $prevIndex, nextIndex $nextIndex")

        val warningCodeNext = weather.hourlyWarningCode.getOrNull(nextIndex) ?: return null
        val warningCodePrev = weather.hourlyWarningCode.getOrNull(prevIndex) ?: return null

        val warningCode = maxOf(warningCodePrev, warningCodeNext)
        val warningLevel = WarningCodeMapper.getWarningLevelFromCode(warningCode)

        Timber.d("getWarningStrAndIcon: Warning $warningCode, level $warningLevel")

        if (warningCode == WarningCode.NO_WARNING || warningLevel == WarningLevel.NONE)
            return null

        return TextWithOneIcon(
            context.getString(WarningString.getWarningStrId(warningCode)),
            WarningIcon.getWarningIconId(warningLevel, textStyle)
        )
    }

    private fun getNextTimeIndex(
        timeMillis: List<Long>,
        nowTimeMillis: Long
    ): Int? {
        val idx = timeMillis.indexOfFirst { it > nowTimeMillis }

        if (idx == -1) {
            Timber.w("getNextTimeIndex: No data available at given time")
            return null
        }
        return idx
    }

    private fun getPrevTimeIndex(
        timeMillis: List<Long>,
        nowTimeMillis: Long
    ): Int? {
        val idx = timeMillis.indexOfLast { it <= nowTimeMillis }

        if (idx == -1) {
            Timber.w("getPrevTimeIndex: No data available at given time")
            return null
        }
        return idx
    }

    private fun getForecastStr(
        context: Context,
        nowTimeMillis: Long,
        weather: WeatherData,
        tempUnit: TempUnit
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
        Timber.d("getForecastStr: Target date $targetDate at index $idx")

        if (idx == -1) {
            Timber.w("getForecastStr: No forecast data available")
            return null
        }
        val weatherCode = weather.dailyWeatherCode.getOrNull(idx) ?: return null
        val tempCelsiusMin = weather.dailyTempMinCelsius.getOrNull(idx) ?: return null
        val tempCelsiusMax = weather.dailyTempMaxCelsius.getOrNull(idx) ?: return null

        val minTempStr = getTemperatureStr(context, tempCelsiusMin, tempUnit, false)
        val maxTempStr = getTemperatureStr(context, tempCelsiusMax, tempUnit, false)

        val weatherStr = context.getString(
            WeatherString.getShortWeatherStrId(weatherCode, isDay = true)
        )
        val dayStr = context.getString(
            if (isToday) R.string.today else R.string.tomorrow
        )
        return "$dayStr $maxTempStr / $minTempStr · $weatherStr"
    }

    private fun getTemperatureStr(
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