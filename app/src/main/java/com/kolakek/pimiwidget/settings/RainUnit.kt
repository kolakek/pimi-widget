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

package com.kolakek.pimiwidget.settings

import android.content.Context
import com.kolakek.pimiwidget.R

enum class RainUnit(private val unitRes: Int) {
    MM(R.string.unit_mm) {
        override fun fromMm(value: Double) = value
    },
    INCH(R.string.unit_in) {
        override fun fromMm(value: Double) = value / 25.4
    };

    abstract fun fromMm(value: Double): Double

    fun unitStr(context: Context) = context.getString(unitRes)
}
