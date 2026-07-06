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

package com.kolakek.pimiwidget.settings

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.kolakek.pimiwidget.utility.WeatherApp

internal object PreferencesHelper {

    internal enum class IconStylePref(val key: String) {
        FLAT_SKETCH(KEY_ICON_STYLE_FLAT_SKETCH),
        TWINKLE_SHADOW(KEY_ICON_STYLE_TWINKLE_SHADOW)
    }

    internal enum class TempUnitPref(val key: String) {
        CELSIUS(KEY_CELSIUS),
        FAHRENHEIT(KEY_FAHRENHEIT)
    }

    internal enum class TextColorPref(val key: String) {
        AUTO(KEY_COLOR_AUTO),
        LIGHT(KEY_COLOR_LIGHT),
        DARK(KEY_COLOR_DARK)
    }

    internal enum class AuxDisplayPref(val key: String) {
        NOTHING(KEY_DISPLAY_NOTHING),
        UPDATE_TIME(KEY_DISPLAY_UPDATE_TIME),
    }

    internal fun getWidgetPreferences(context: Context): WidgetPreferences {
        val textColorPref = getTextColorPreference(context)
        val iconStylePref = getIconStylePreference(context)
        val tempUnitPref = getTempUnitPreference(context)
        val auxDisplayPref = getAuxDisplayPreference(context)
        val weatherApp = getWeatherApp(context)

        val isLightText = when (textColorPref) {
            TextColorPref.AUTO ->
                WallpaperManager
                    .getInstance(context)
                    .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                    ?.colorHints
                    ?.let { it and WallpaperColors.HINT_SUPPORTS_DARK_TEXT == 0 } ?: true
            TextColorPref.LIGHT -> true
            TextColorPref.DARK -> false
        }
        val iconStyle = when (iconStylePref) {
            IconStylePref.FLAT_SKETCH ->
                if (isLightText) IconStyle.FLAT_SKETCH_LIGHT else IconStyle.FLAT_SKETCH_DARK

            IconStylePref.TWINKLE_SHADOW ->
                if (isLightText) IconStyle.TWINKLE_SHADOW_LIGHT else IconStyle.TWINKLE_SHADOW_DARK
        }
        val textStyle = when (isLightText to iconStylePref) {
            true to IconStylePref.FLAT_SKETCH -> TextStyle.LIGHT
            true to IconStylePref.TWINKLE_SHADOW -> TextStyle.LIGHT_SHADOW
            else -> TextStyle.DARK
        }
        val tempUnit = when (tempUnitPref) {
            TempUnitPref.CELSIUS -> TempUnit.CELSIUS
            TempUnitPref.FAHRENHEIT -> TempUnit.FAHRENHEIT
        }
        val auxDisplay = when (auxDisplayPref) {
            AuxDisplayPref.NOTHING -> AuxDisplay.NOTHING
            AuxDisplayPref.UPDATE_TIME -> AuxDisplay.UPDATE_TIME
        }

        return WidgetPreferences(
            showWeather = getWeatherPreference(context),
            useLocationFallback = getLocationFallbackPreference(context),
            showDailyForecast = getDailyForecastPreference(context),
            showWeatherWarning = getWeatherWarningPreference(context),
            tempUnit = tempUnit,
            iconStyle = iconStyle,
            textStyle = textStyle,
            auxDisplay = auxDisplay,
            weatherApp = weatherApp
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

    internal fun setWeatherAppPreference(context: Context, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(KEY_WEATHER_APP_LIST, value)
        }
    }

    private fun getDailyForecastPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_DAILY_FORECAST, true)
    }

    private fun getWeatherWarningPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_WEATHER_WARNING, true)
    }

    private fun getTextColorPreference(context: Context): TextColorPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_TEXT_COLOR_LIST, null)
        return TextColorPref.entries.find { it.key == key } ?: TextColorPref.AUTO
    }

    private fun getIconStylePreference(context: Context): IconStylePref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_ICON_STYLE_LIST, null)
        return IconStylePref.entries.find { it.key == key } ?: IconStylePref.FLAT_SKETCH

    }

    private fun getTempUnitPreference(context: Context): TempUnitPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_TEMP_UNITS, null)
        return TempUnitPref.entries.find { it.key == key } ?: TempUnitPref.CELSIUS
    }

    private fun getAuxDisplayPreference(context: Context): AuxDisplayPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_AUX_DISPLAY_LIST, null)
        return AuxDisplayPref.entries.find { it.key == key } ?: AuxDisplayPref.NOTHING
    }

    private fun getLocationFallbackPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_LOCATION_FALLBACK, true)
    }

    private fun getWeatherApp(context: Context): WeatherApp {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_WEATHER_APP_LIST, null)
        return WeatherApp.entries.find { it.key == key } ?: WeatherApp.NONE
    }
}
