package com.kolakek.pimiwidget.settings

import androidx.annotation.StringRes
import com.kolakek.pimiwidget.R

enum class TextColorPref(val key: String, @StringRes val labelRes: Int) {
    AUTO(KEY_COLOR_AUTO, R.string.config_option_auto),
    LIGHT(KEY_COLOR_LIGHT, R.string.config_color_light),
    DARK(KEY_COLOR_DARK, R.string.config_color_dark);

    companion object {
        val DEFAULT = AUTO
        fun fromKey(key: String?) = entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}
