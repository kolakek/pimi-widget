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

package com.kolakek.pimiwidget.widget

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

fun getTextColorPreference(context: Context): String {
    return PreferenceManager.getDefaultSharedPreferences(context)
        .getString(KEY_TEXT_COLOR_LIST, KEY_COLOR_LIGHT) ?: KEY_COLOR_LIGHT
}

fun getIconColorPreference(context: Context): String {
    return PreferenceManager.getDefaultSharedPreferences(context)
        .getString(KEY_ICON_COLOR_LIST, KEY_COLOR_LIGHT) ?: KEY_COLOR_LIGHT
}

fun getTempPreference(context: Context): String {
    return PreferenceManager.getDefaultSharedPreferences(context)
        .getString(KEY_TEMP_UNITS, KEY_CELSIUS) ?: KEY_CELSIUS
}

fun getWeatherPreference(context: Context): Boolean {
    return PreferenceManager.getDefaultSharedPreferences(context)
        .getBoolean(KEY_WEATHER_SWITCH, false)
}

fun setWeatherPreference(context: Context, value: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(context).edit {
        putBoolean(KEY_WEATHER_SWITCH, value)
    }
}
