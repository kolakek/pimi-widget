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
    DYNAMIC_LIGHT(
        R.color.widget_color_text_dynamic_light,
        R.color.widget_color_temp_dynamic_light,
        R.color.widget_color_background_dynamic_light,
        R.color.widget_color_icon_background_dynamic_light
    ),
    DYNAMIC_DARK(
        R.color.widget_color_text_dynamic_dark,
        R.color.widget_color_temp_dynamic_dark,
        R.color.widget_color_background_dynamic_dark,
        R.color.widget_color_icon_background_dynamic_dark
    ),
    NEUTRAL(
        R.color.widget_color_text_neutral,
        R.color.widget_color_temp_neutral,
        R.color.widget_color_background_neutral,
        R.color.widget_color_icon_background_neutral
    ),
    NEUTRAL_LIGHT(
        R.color.widget_color_text_neutral_light,
        R.color.widget_color_temp_neutral_light,
        R.color.widget_color_background_neutral_light,
        R.color.widget_color_icon_background_neutral_light
    ),
    NEUTRAL_DARK(
        R.color.widget_color_text_neutral_dark,
        R.color.widget_color_temp_neutral_dark,
        R.color.widget_color_background_neutral_dark,
        R.color.widget_color_icon_background_neutral_dark
    )
}

