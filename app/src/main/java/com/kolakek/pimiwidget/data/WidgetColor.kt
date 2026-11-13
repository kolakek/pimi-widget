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
        R.color.dynamic_text_color,
        R.color.dynamic_temp_color,
        R.color.dynamic_background_color,
        R.color.dynamic_icon_background_color
    ),
    SYSTEM(
        R.color.dynamic_text_color,
        R.color.dynamic_temp_color,
        R.color.dynamic_background_color,
        R.color.dynamic_icon_background_color
    ),
    LIGHT(
        R.color.dynamic_text_color,
        R.color.dynamic_temp_color,
        R.color.dynamic_background_color,
        R.color.dynamic_icon_background_color
    ),
    DARK(
        R.color.dynamic_text_color,
        R.color.dynamic_temp_color,
        R.color.dynamic_background_color,
        R.color.dynamic_icon_background_color
    )
}

