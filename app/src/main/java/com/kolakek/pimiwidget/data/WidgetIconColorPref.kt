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
import androidx.core.content.edit
import com.kolakek.pimiwidget.R

enum class WidgetIconColorPref(val key: String, @StringRes val labelRes: Int) {
    AUTO(KEY_ICON_COLOR_AUTO, R.string.config_option_auto),
    LIGHT(KEY_ICON_COLOR_LIGHT, R.string.config_icon_color_morning),
    DARK(KEY_ICON_COLOR_DARK, R.string.config_icon_color_evening);

    fun save(prefs: SharedPreferences) = prefs.edit { putString(KEY_ICON_COLOR_LIST, key) }

    companion object {
        val DEFAULT = AUTO
        fun fromKey(key: String?) = entries.firstOrNull { it.key == key } ?: DEFAULT
        fun fromPrefs(prefs: SharedPreferences) = fromKey(
            prefs.getString(KEY_ICON_COLOR_LIST, null)
        )
    }
}
