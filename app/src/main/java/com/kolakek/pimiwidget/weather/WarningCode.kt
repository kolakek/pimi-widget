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

import androidx.annotation.StringRes
import com.kolakek.pimiwidget.R

// Ordering expresses priority, highest ... lowest
enum class WarningCode(val level: WarningLevel, @StringRes val labelRes: Int) {
    EXTREME_TSTORM(WarningLevel.EXTREME, R.string.warn_tstorm_extreme),
    EXTREME_RAIN(WarningLevel.EXTREME, R.string.warn_rain_extreme),
    EXTREME_GUSTS(WarningLevel.EXTREME, R.string.warn_gusts_extreme),
    SEVERE_TSTORM(WarningLevel.SEVERE, R.string.warn_tstorm_severe),
    SEVERE_RAIN(WarningLevel.SEVERE, R.string.warn_rain_severe),
    SEVERE_GUSTS(WarningLevel.SEVERE, R.string.warn_gusts_severe),
    EXTREME_HEAT(WarningLevel.EXTREME, R.string.warn_heat_extreme),
    SEVERE_HEAT(WarningLevel.SEVERE, R.string.warn_heat_severe),
    EXTREME_UV(WarningLevel.EXTREME, R.string.warn_uv_extreme),
    SEVERE_UV(WarningLevel.SEVERE, R.string.warn_uv_severe),
    NO_WARNING(WarningLevel.NONE, R.string.warn_no_warning)
}
