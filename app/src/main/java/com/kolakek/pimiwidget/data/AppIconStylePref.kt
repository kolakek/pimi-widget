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

import android.content.SharedPreferences
import androidx.annotation.StringRes
import com.kolakek.pimiwidget.R

enum class AppIconStylePref(val key: String, @StringRes val labelRes: Int) {
    FLAT_SKETCH(KEY_APP_ICON_STYLE_FLAT_SKETCH, R.string.config_icon_style_outlined),
    TWINKLE_SHADOW(KEY_APP_ICON_STYLE_TWINKLE_SHADOW, R.string.config_icon_style_filled),
    FOLLOW_WIDGET(KEY_APP_ICON_STYLE_WIDGET, R.string.config_option_follow_widget);

    companion object {
        val DEFAULT = FLAT_SKETCH
        fun fromKey(key: String?) = entries.firstOrNull { it.key == key } ?: DEFAULT
        fun fromPrefs(prefs: SharedPreferences) = fromKey(
            prefs.getString(KEY_APP_ICON_STYLE_LIST, null)
        )
    }
}
