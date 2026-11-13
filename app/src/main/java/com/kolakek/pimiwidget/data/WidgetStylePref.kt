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

import androidx.annotation.StringRes
import com.kolakek.pimiwidget.R

enum class WidgetStylePref(val key: String, @StringRes val labelRes: Int) {
    CLASSIC(KEY_WIDGET_STYLE_CLASSIC, R.string.config_widget_style_classic),
    MODERN(KEY_WIDGET_STYLE_MODERN, R.string.config_widget_style_modern),
    SOLID(KEY_WIDGET_STYLE_SOLID, R.string.config_widget_style_solid);

    companion object {
        val DEFAULT = SOLID
        fun fromKey(key: String?) = entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}
