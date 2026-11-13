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

