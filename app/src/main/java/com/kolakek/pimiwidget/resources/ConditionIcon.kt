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

package com.kolakek.pimiwidget.resources

import com.kolakek.pimiwidget.R
import java.time.LocalDate
import java.time.ZoneId

object ConditionIcon {

    fun getWindIconId(): Int {
        return R.drawable.mw
    }

    fun getSunTrackIconId(
        timeMillis: Long,
        sunriseMillis: Long,
        sunsetMillis: Long
    ): Int {
        return when {
            timeMillis < sunriseMillis -> {
                val startOfDay = LocalDate.now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                val preSunrise = sunriseMillis - 15 * 60 * 1000L
                val step = (preSunrise - startOfDay).coerceAtLeast(0L) / 6
                when {
                    timeMillis > preSunrise -> R.drawable.ms_d
                    timeMillis < startOfDay + 1 * step -> R.drawable.ms_1
                    timeMillis < startOfDay + 2 * step -> R.drawable.ms_2
                    timeMillis < startOfDay + 3 * step -> R.drawable.ms_3
                    timeMillis < startOfDay + 4 * step -> R.drawable.ms_4
                    timeMillis < startOfDay + 5 * step -> R.drawable.ms_5
                    else -> R.drawable.ms_6
                }
            }

            timeMillis > sunsetMillis -> {
                val endOfDay = LocalDate.now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .plusDays(1)
                    .toInstant()
                    .toEpochMilli() - 1
                val postSunset = sunsetMillis + 15 * 60 * 1000L
                val step = (endOfDay - postSunset).coerceAtLeast(0L) / 6
                when {
                    timeMillis < postSunset -> R.drawable.ms_n
                    timeMillis < sunsetMillis + 1 * step -> R.drawable.ms_20
                    timeMillis < sunsetMillis + 2 * step -> R.drawable.ms_21
                    timeMillis < sunsetMillis + 3 * step -> R.drawable.ms_22
                    timeMillis < sunsetMillis + 4 * step -> R.drawable.ms_23
                    timeMillis < sunsetMillis + 5 * step -> R.drawable.ms_24
                    else -> R.drawable.ms_25
                }
            }

            else -> {
                val postSunrise = sunriseMillis + 15 * 60 * 1000L
                val preSunset = sunsetMillis - 15 * 60 * 1000L
                val step = (preSunset - postSunrise).coerceAtLeast(0L) / 13
                when {
                    timeMillis < postSunrise -> R.drawable.ms_r
                    timeMillis > preSunset -> R.drawable.ms_s
                    timeMillis < postSunrise + 1 * step -> R.drawable.ms_7
                    timeMillis < postSunrise + 2 * step -> R.drawable.ms_8
                    timeMillis < postSunrise + 3 * step -> R.drawable.ms_9
                    timeMillis < postSunrise + 4 * step -> R.drawable.ms_10
                    timeMillis < postSunrise + 5 * step -> R.drawable.ms_11
                    timeMillis < postSunrise + 6 * step -> R.drawable.ms_12
                    timeMillis < postSunrise + 7 * step -> R.drawable.ms_13
                    timeMillis < postSunrise + 8 * step -> R.drawable.ms_14
                    timeMillis < postSunrise + 9 * step -> R.drawable.ms_15
                    timeMillis < postSunrise + 10 * step -> R.drawable.ms_16
                    timeMillis < postSunrise + 11 * step -> R.drawable.ms_17
                    timeMillis < postSunrise + 12 * step -> R.drawable.ms_18
                    else -> R.drawable.ms_19
                }
            }
        }
    }

    fun getHumidityIconId(humidity: Double): Int {
        return when (humidity.toInt()) {
            in 0 .. 5 -> R.drawable.mh_0
            in 5 .. 15 -> R.drawable.mh_10
            in 15 .. 25 -> R.drawable.mh_20
            in 25 .. 35 -> R.drawable.mh_30
            in 35 .. 45 -> R.drawable.mh_40
            in 45 .. 55 -> R.drawable.mh_50
            in 55 .. 65 -> R.drawable.mh_60
            in 65 .. 75 -> R.drawable.mh_70
            in 75 .. 85 -> R.drawable.mh_80
            in 85 .. 95 -> R.drawable.mh_90
            else -> R.drawable.mh_100
        }
    }

    fun getUvIndexIconId(uvIndex: Double): Int {
        return when (uvIndex.toInt()) {
            0 -> R.drawable.mu_0
            1 -> R.drawable.mu_1
            2 -> R.drawable.mu_2
            3 -> R.drawable.mu_3
            4 -> R.drawable.mu_4
            5 -> R.drawable.mu_5
            6 -> R.drawable.mu_6
            7 -> R.drawable.mu_7
            8 -> R.drawable.mu_8
            9 -> R.drawable.mu_9
            10 -> R.drawable.mu_10
            else -> R.drawable.mu_11
        }
    }

    fun getPressureIconId(pressure: Double): Int {
        return when (pressure.toInt()) {
            in 0..970 -> R.drawable.mp_1
            in 970..985 -> R.drawable.mp_2
            in 985..998 -> R.drawable.mp_3
            in 998..1005 -> R.drawable.mp_4
            in 1005..1009 -> R.drawable.mp_5
            in 1009..1013 -> R.drawable.mp_6
            in 1013..1017 -> R.drawable.mp_7
            in 1017..1021 -> R.drawable.mp_8
            in 1021..1027 -> R.drawable.mp_9
            in 1027..1035 -> R.drawable.mp_10
            in 1035..1045 -> R.drawable.mp_11
            else -> R.drawable.mp_12
        }
    }
}
