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

import kotlinx.serialization.Serializable

@Serializable
data class WeatherData (
    val minutelyWeatherCode: List<WeatherCode>,
    val minutelyTempCelsius: List<Double>,
    val minutelyApparentCelsius: List<Double>,
    val minutelyWindSpeedKmh: List<Double>,
    val minutelyWindGustsKmh: List<Double>,
    val minutelyWindDirectionDeg: List<Double>,
    val minutelyHumidity: List<Double>,
    val minutelyDewPointCelsius: List<Double>,
    val minutelyUvIndex: List<Double>,
    val minutelyUvIndexClearSky: List<Double>,
    val minutelyTimeMillis: List<Long>,
    val minutelyIsDay: List<Boolean>,
    val hourlyWeatherCode: List<WeatherCode>,
    val hourlyTempCelsius: List<Double>,
    val hourlyIsDay: List<Boolean>,
    val hourlyWarningCode: List<WarningCode>,
    val hourlyTimeMillis: List<Long>,
    val dailyWeatherCode: List<WeatherCode>,
    val dailyTempMinCelsius: List<Double>,
    val dailyTempMaxCelsius: List<Double>,
    val dailyTimeMillis: List<Long>,
    val timeMillis: Long,
    val place: String
) {
    fun currentTempCelsius(): Double? {
        return minutelyTempCelsius.getOrNull(currentMinutelyIndex())
    }

    fun currentApparentTempCelsius(): Double? {
        return minutelyApparentCelsius.getOrNull(currentMinutelyIndex())
    }

    fun currentWeatherCode(): WeatherCode? {
        return minutelyWeatherCode.getOrNull(currentMinutelyIndex())
    }

    fun currentWindSpeedKmh(): Double? {
        return minutelyWindSpeedKmh.getOrNull(currentMinutelyIndex())
    }

    fun currentWindGustsKmh(): Double? {
        return minutelyWindGustsKmh.getOrNull(currentMinutelyIndex())
    }

    fun currentWindDirectionDeg(): Double? {
        return minutelyWindDirectionDeg.getOrNull(currentMinutelyIndex())
    }

    fun currentHumidity(): Double? {
        return minutelyHumidity.getOrNull(currentMinutelyIndex())
    }

    fun currentDewPointCelsius(): Double? {
        return minutelyDewPointCelsius.getOrNull(currentMinutelyIndex())
    }

    fun currentUvIndex(): Double? {
        return minutelyUvIndex.getOrNull(currentMinutelyIndex())
    }

    fun currentUvIndexClearSky(): Double? {
        return minutelyUvIndexClearSky.getOrNull(currentMinutelyIndex())
    }

    fun currentIsDay(): Boolean? {
        return minutelyIsDay.getOrNull(currentMinutelyIndex())
    }

    fun nextHourlyWarningCode(): WarningCode? {
        return hourlyWarningCode.getOrNull(nextHourlyIndex())
    }

    fun todayMinTempCelsius(): Double? {
        return dailyTempMinCelsius.getOrNull(todayIndex())
    }

    fun todayMaxTempCelsius(): Double? {
        return dailyTempMaxCelsius.getOrNull(todayIndex())
    }

    fun currentMinutelyIndex(): Int {
        return minutelyTimeMillis.indexOfFirst { it > System.currentTimeMillis() }
    }

    fun nextHourlyIndex(): Int {
        return hourlyTimeMillis.indexOfFirst { it > System.currentTimeMillis() }
    }

    fun todayIndex(): Int {
        return dailyTimeMillis.indexOfLast { it < System.currentTimeMillis() }
    }
}
