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

internal object PreferencesHelper {

    enum class IconStyle(val key: String) {
        FLAT_OUTLINED(KEY_ICON_STYLE_OUTLINED) // ToDo
    }

    enum class TempUnit(val key: String) {
        CELSIUS(KEY_CELSIUS),
        FAHRENHEIT(KEY_FAHRENHEIT)
    }

    enum class TextColor(val key: String) {
        AUTO(KEY_COLOR_AUTO),
        LIGHT(KEY_COLOR_LIGHT),
        DARK(KEY_COLOR_DARK)
    }

    internal fun getWidgetPreferences(context: Context): WidgetPreferences {
        return WidgetPreferences(
            showWeather = getWeatherPreference(context),
            showForecast = getDailyForecastPreference(context),
            tempUnit = getTempUnitPreference(context),
            iconStyle = getIconStylePreference(context),
            textColor = getTextColorPreference(context)
        )
    }

    internal fun getWeatherPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_WEATHER_SWITCH, false)
    }

    internal fun setWeatherPreference(context: Context, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(KEY_WEATHER_SWITCH, value)
        }
    }

    private fun getDailyForecastPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_DAILY_FORECAST, true)
    }

    private fun getTextColorPreference(context: Context): TextColor {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_TEXT_COLOR_LIST, null)
        return TextColor.entries.find { it.key == key } ?: TextColor.AUTO
    }

    private fun getIconStylePreference(context: Context): IconStyle {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_ICON_STYLE_LIST, null)
        return IconStyle.entries.find { it.key == key } ?: IconStyle.FLAT_OUTLINED

    }

    private fun getTempUnitPreference(context: Context): TempUnit {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_TEMP_UNITS, null)
        return TempUnit.entries.find { it.key == key } ?: TempUnit.CELSIUS
    }
}
