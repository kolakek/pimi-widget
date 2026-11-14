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
import com.kolakek.pimiwidget.R

enum class WidgetColor(
    @ColorRes val textColorRes: Int,
    @ColorRes val tempTextColorRes: Int,
    @ColorRes val backgroundColorRes: Int,
    @ColorRes val iconBackgroundColorRes: Int
) {
    DYNAMIC(
        R.color.widget_color_text_dynamic,
        R.color.widget_color_temp_dynamic,
        R.color.widget_color_background_dynamic,
        R.color.widget_color_icon_background_dynamic
    ),
    SYSTEM(
        R.color.widget_color_text_system,
        R.color.widget_color_temp_system,
        R.color.widget_color_background_system,
        R.color.widget_color_icon_background_system
    ),
    LIGHT(
        R.color.widget_color_text_light,
        R.color.widget_color_temp_light,
        R.color.widget_color_background_light,
        R.color.widget_color_icon_background_light
    ),
    DARK(
        R.color.widget_color_text_dark,
        R.color.widget_color_temp_dark,
        R.color.widget_color_background_dark,
        R.color.widget_color_icon_background_dark
    )
}

