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
import com.kolakek.pimiwidget.weather.WarningCode

object WarningString {

    fun getWarningStrId(warningCode: WarningCode): Int {
        return when (warningCode) {
            WarningCode.NO_WARNING -> R.string.no_warning
            WarningCode.SEVERE_UV -> R.string.severe_uv
            WarningCode.EXTREME_UV -> R.string.extreme_uv
            WarningCode.SEVERE_HEAT -> R.string.severe_heat
            WarningCode.EXTREME_HEAT -> R.string.extreme_heat
            WarningCode.SEVERE_RAIN -> R.string.severe_rain
            WarningCode.EXTREME_RAIN -> R.string.extreme_rain
        }
    }
}
