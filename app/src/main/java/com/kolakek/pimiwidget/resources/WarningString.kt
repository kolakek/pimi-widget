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
            WarningCode.NO_WARNING -> R.string.warn_no_warning
            WarningCode.SEVERE_UV -> R.string.warn_uv_severe
            WarningCode.EXTREME_UV -> R.string.warn_uv_extreme
            WarningCode.SEVERE_HEAT -> R.string.warn_heat_severe
            WarningCode.EXTREME_HEAT -> R.string.warn_heat_extreme
            WarningCode.SEVERE_RAIN -> R.string.warn_rain_severe
            WarningCode.EXTREME_RAIN -> R.string.warn_rain_extreme
            WarningCode.SEVERE_GUSTS -> R.string.warn_gusts_severe
            WarningCode.EXTREME_GUSTS -> R.string.warn_gusts_extreme
            WarningCode.SEVERE_TSTORM -> R.string.warn_tstorm_severe
            WarningCode.EXTREME_TSTORM -> R.string.warn_tstorm_extreme
        }
    }
}
