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

enum class WindUnit(private val unitRes: Int, val format: String) {
    KMH(R.string.unit_kmh, "%.0f") {
        override fun fromKmh(value: Double) = value
    },
    MS(R.string.unit_ms, "%.1f") {
        override fun fromKmh(value: Double) = value / 3.6
    },
    MPH(R.string.unit_mph, "%.0f") {
        override fun fromKmh(value: Double) = value * 0.621371
    },
    KNOT(R.string.unit_knot, "%.1f") {
        override fun fromKmh(value: Double) = value / 1.852
    };

    abstract fun fromKmh(value: Double): Double

    fun unitStr(context: Context) = context.getString(unitRes)
}
