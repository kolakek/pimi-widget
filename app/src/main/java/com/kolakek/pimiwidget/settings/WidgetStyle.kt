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

import androidx.annotation.ColorRes
import com.kolakek.pimiwidget.R

enum class WidgetStyle(val needsShadow: Boolean) {
    CLASSIC(false),
    CLASSIC_SHADOW(true),
    MODERN(false),
    MODERN_SHADOW(true),
    SOLID(false);

    @ColorRes
    fun textColor(textColor: TextColor): Int = when (this) {
        SOLID -> textColor.colorRes // todo
        CLASSIC, CLASSIC_SHADOW, MODERN, MODERN_SHADOW -> textColor.colorRes
    }

    @ColorRes
    fun tempTextColor(textColor: TextColor): Int = when (this) {
        SOLID -> R.color.dynamic_temp_color // todo
        CLASSIC, CLASSIC_SHADOW, MODERN, MODERN_SHADOW -> textColor.colorRes
    }

    @ColorRes
    fun backgroundColor(): Int? = when (this) {
        SOLID -> R.color.dynamic_background_color // todo
        CLASSIC, CLASSIC_SHADOW, MODERN, MODERN_SHADOW -> null
    }

    @ColorRes
    fun iconBackgroundColor(): Int? = when (this) {
        SOLID -> R.color.dynamic_icon_background_color // todo
        CLASSIC, CLASSIC_SHADOW, MODERN, MODERN_SHADOW -> null
    }
}
