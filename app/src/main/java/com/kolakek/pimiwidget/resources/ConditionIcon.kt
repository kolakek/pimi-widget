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

object ConditionIcon {

    fun getWindIconId(): Int {
        return R.drawable.mw
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
            in 0..963 -> R.drawable.mp_963
            in 963..973 -> R.drawable.mp_973
            in 973..983 -> R.drawable.mp_983
            in 983..993 -> R.drawable.mp_993
            in 993..1003 -> R.drawable.mp_1003
            in 1003..1013 -> R.drawable.mp_1013
            in 1013..1023 -> R.drawable.mp_1023
            in 1023..1033 -> R.drawable.mp_1033
            in 1033..1043 -> R.drawable.mp_1043
            in 1043..1053 -> R.drawable.mp_1053
            in 1053..1063 -> R.drawable.mp_1063
            else -> R.drawable.mp_1073
        }
    }
}
