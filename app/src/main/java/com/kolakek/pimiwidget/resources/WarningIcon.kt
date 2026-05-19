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
import com.kolakek.pimiwidget.weather.WarningLevel
import com.kolakek.pimiwidget.widget.TextStyle

object WarningIcon {

    fun getWarningIconId(
        warningLevel: WarningLevel,
        textStyle: TextStyle
    ): Int {
        return when (warningLevel) {
            WarningLevel.SEVERE ->
                when (textStyle) {
                    TextStyle.DARK -> R.drawable.warn_sevr_dark
                    TextStyle.LIGHT -> R.drawable.warn_sevr_light
                    TextStyle.LIGHT_SHADOW -> R.drawable.warn_sevr_light_shadow
                }

            WarningLevel.EXTREME ->
                when (textStyle) {
                    TextStyle.DARK -> R.drawable.warn_extr
                    TextStyle.LIGHT -> R.drawable.warn_extr
                    TextStyle.LIGHT_SHADOW -> R.drawable.warn_extr_shadow
                }
        }
    }
}