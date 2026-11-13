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

package com.kolakek.pimiwidget.data

import androidx.annotation.ColorRes

enum class WidgetStyle(val needsShadow: Boolean) {
    CLASSIC(false),
    CLASSIC_SHADOW(true),
    MODERN(false),
    MODERN_SHADOW(true),
    SOLID(false);

    @ColorRes
    fun textColor(textColor: TextColor, widgetColor: WidgetColor): Int = when (this) {
        SOLID -> widgetColor.textColorRes
        CLASSIC, CLASSIC_SHADOW, MODERN, MODERN_SHADOW -> textColor.colorRes
    }

    @ColorRes
    fun tempTextColor(textColor: TextColor, widgetColor: WidgetColor): Int = when (this) {
        SOLID -> widgetColor.tempTextColorRes
        CLASSIC, CLASSIC_SHADOW, MODERN, MODERN_SHADOW -> textColor.colorRes
    }

    @ColorRes
    fun backgroundColor(widgetColor: WidgetColor): Int? = when (this) {
        SOLID -> widgetColor.backgroundColorRes
        CLASSIC, CLASSIC_SHADOW, MODERN, MODERN_SHADOW -> null
    }

    @ColorRes
    fun iconBackgroundColor(widgetColor: WidgetColor): Int? = when (this) {
        SOLID -> widgetColor.iconBackgroundColorRes
        CLASSIC, CLASSIC_SHADOW, MODERN, MODERN_SHADOW -> null
    }
}
