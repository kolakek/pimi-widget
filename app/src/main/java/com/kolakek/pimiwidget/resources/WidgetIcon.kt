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
import com.kolakek.pimiwidget.settings.TextStyle

enum class WidgetIcon {
    ALARM,
    BIRTHDAY;

    fun id(textStyle: TextStyle): Int {
        return when (this) {
            ALARM -> when (textStyle) {
                TextStyle.DARK -> R.drawable.ic_alarm_dark
                TextStyle.LIGHT -> R.drawable.ic_alarm_light
                TextStyle.LIGHT_SHADOW -> R.drawable.ic_alarm_light_shadow
            }
            BIRTHDAY -> when (textStyle) {
                TextStyle.DARK -> R.drawable.ic_birthday_dark
                TextStyle.LIGHT -> R.drawable.ic_birthday_light
                TextStyle.LIGHT_SHADOW -> R.drawable.ic_birthday_light_shadow
            }
        }
    }
}
