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
        return when (humidity) {
            in 0.0 .. 5.0 -> R.drawable.mh_0
            in 5.0 .. 15.0 -> R.drawable.mh_10
            in 15.0 .. 25.0 -> R.drawable.mh_20
            in 25.0 .. 35.0 -> R.drawable.mh_30
            in 35.0 .. 45.0 -> R.drawable.mh_40
            in 45.0 .. 55.0 -> R.drawable.mh_50
            in 55.0 .. 65.0 -> R.drawable.mh_60
            in 65.0 .. 75.0 -> R.drawable.mh_70
            in 75.0 .. 85.0 -> R.drawable.mh_80
            in 85.0 .. 95.0 -> R.drawable.mh_90
            else -> R.drawable.mh_100
        }
    }
}
