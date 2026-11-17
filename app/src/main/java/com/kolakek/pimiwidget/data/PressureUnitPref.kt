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

enum class PressureUnitPref(val key: String, @StringRes val labelRes: Int) {
    AUTO(KEY_PRESSURE_UNIT_AUTO, R.string.config_option_follow_system),
    HPA(KEY_PRESSURE_UNIT_HPA, R.string.config_unit_hectopascal),
    MB(KEY_PRESSURE_UNIT_MB, R.string.config_unit_millibar),
    MMHG(KEY_PRESSURE_UNIT_MMHG, R.string.config_unit_mm_mercury),
    INHG(KEY_PRESSURE_UNIT_INHG, R.string.config_unit_inches_mercury);

    companion object {
        val DEFAULT = AUTO
        fun fromKey(key: String?) = entries.firstOrNull { it.key == key } ?: DEFAULT
        fun fromPrefs(prefs: SharedPreferences) = fromKey(
            prefs.getString(KEY_PRESSURE_UNIT_LIST, null)
        )
    }
}
