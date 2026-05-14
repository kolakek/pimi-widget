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
import com.kolakek.pimiwidget.resources.IconStyles
import com.kolakek.pimiwidget.resources.WeatherIcons
import com.kolakek.pimiwidget.resources.WeatherStrings
import com.kolakek.pimiwidget.weather.WeatherData
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId

internal object WeatherFormatter {

    internal fun getWeatherStrAndIcon(
        context: Context,
        weather: WeatherData,
        showForecast: Boolean,
        tempUnitPref: PreferencesHelper.TempUnit,
        iconStylePref: PreferencesHelper.IconStyle,
        lightColor: Boolean
    ): TextWithIcon? {
        val nowTimeMillis = System.currentTimeMillis()
        val currentIndex = getCurrentIndex(weather.minutelyTimeMillis, nowTimeMillis) ?: return null

        val tempCelsius = weather.minutelyTempCelsius.getOrNull(currentIndex) ?: return null
        val weatherCode = weather.minutelyWeatherCode.getOrNull(currentIndex) ?: return null
        val cloudCover = weather.minutelyCloudCover.getOrNull(currentIndex) ?: return null
        val cape = weather.minutelyCape.getOrNull(currentIndex) ?: return null
        val isDay = weather.minutelyIsDay.getOrNull(currentIndex) ?: return null

        val temperatureStr = getTemperatureStr(context, tempCelsius, tempUnitPref)

        val iconStyle = when (iconStylePref) {
            PreferencesHelper.IconStyle.FLAT_OUTLINED ->
                if (lightColor) IconStyles.FLAT_OUTLINED_LIGHT else IconStyles.FLAT_OUTLINED_DARK
            PreferencesHelper.IconStyle.SOLID_3D ->
                if (lightColor) IconStyles.SOLID_3D_LIGHT else IconStyles.SOLID_3D_DARK
        }
        val weatherIconId = WeatherIcons.getWeatherIconId(
            weatherCode,
            isDay,
            cloudCover,
            cape,
            iconStyle
        ) ?: return null

        val forecastStr = if (showForecast) {
            getForecastStr(
                context,
                nowTimeMillis,
                weather,
                tempUnitPref
            )
        } else null

        val weatherStr = forecastStr?.let { temperatureStr + it } ?: temperatureStr

        return TextWithIcon(weatherStr, weatherIconId)
    }

    private fun getCurrentIndex(
        minutelyTimeMillis: List<Long>,
        nowTimeMillis: Long
    ): Int? {
        val idx = minutelyTimeMillis.indexOfFirst { it > nowTimeMillis }

        if (idx == -1) {
            Timber.w("getCurrentIndex: No data available at current time")
            return null
        }
        Timber.d("getCurrentIndex: Current index $idx")
        return idx
    }

    private fun getForecastStr(
        context: Context,
        nowTimeMillis: Long,
        weather: WeatherData,
        tempUnitPref: PreferencesHelper.TempUnit
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
        val tempCelsiusMin = weather.dailyTempMinCelsius.getOrNull(idx) ?: return null
        val tempCelsiusMax = weather.dailyTempMaxCelsius.getOrNull(idx) ?: return null
        val cloudCoverMean = weather.dailyCloudCoverMean.getOrNull(idx) ?: return null
        val weatherCode = weather.dailyWeatherCode.getOrNull(idx) ?: return null
        val capeMax = weather.dailyCapeMax.getOrNull(idx) ?: return null

        val minTempStr = getTemperatureStr(context, tempCelsiusMin, tempUnitPref, false)
        val maxTempStr = getTemperatureStr(context, tempCelsiusMax, tempUnitPref, false)

        val weatherStrId = WeatherStrings.getShortWeatherStrId(
            weatherCode,
            isDay = 1,
            cloudCoverMean,
            capeMax
        ) ?: return null

        val dayStrId = if (isToday) R.string.today else R.string.tomorrow

        return context.getString(
            R.string.forecast_line,
            context.getString(dayStrId),
            context.getString(weatherStrId),
            maxTempStr,
            minTempStr
        )
    }

    private fun getTemperatureStr(
        context: Context,
        tempCelsius: Double,
        tempUnitPref: PreferencesHelper.TempUnit,
        fullUnit: Boolean = true
    ): String {
        val isFahrenheit = (tempUnitPref == PreferencesHelper.TempUnit.FAHRENHEIT)
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
