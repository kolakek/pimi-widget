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
    val minutelyTempCelsius: List<Double>,
    val minutelyVisibility: List<Double>,
    val minutelyCape: List<Double>,
    val minutelyWmoCode: List<Int>,
    val minutelyTimeMillis: List<Long>,
    val minutelyIsDay: List<Boolean>,
    val minutelyCloudCover: List<Int>,
    val minutelyPrecipProb: List<Int>,
    val hourlyTempCelsius: List<Double>,
    val hourlyWmoCode: List<Int>,
    val hourlyTimeMillis: List<Long>,
    val hourlyIsDay: List<Boolean>,
    val dailyTempMinCelsius: List<Double>,
    val dailyTempMaxCelsius: List<Double>,
    val dailyVisibilityMean: List<Double>,
    val dailyCapeMax: List<Double>,
    val dailyCloudCoverMean: List<Int>,
    val dailyPrecipProbMax: List<Int>,
    val dailyWmoCode: List<Int>,
    val dailyTimeMillis: List<Long>
)