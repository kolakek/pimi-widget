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

object ConditionString {

    fun getUvIndexStringId(uvIndex: Double): Int {
        return when (uvIndex.toInt()) {
            0 -> R.string.app_text_uv_0
            in 1 .. 2 -> R.string.app_text_uv_1_2
            in 3 .. 5 -> R.string.app_text_uv_3_4_5
            in 6 .. 7 -> R.string.app_text_uv_6_7
            in 8 .. 10 -> R.string.app_text_uv_8_9_10
            else -> R.string.app_text_uv_11
        }
    }
}
