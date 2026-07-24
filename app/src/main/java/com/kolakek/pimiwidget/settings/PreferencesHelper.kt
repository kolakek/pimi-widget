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
import androidx.core.text.util.LocalePreferences
import androidx.preference.PreferenceManager
import com.kolakek.pimiwidget.utility.WeatherApp

object PreferencesHelper {

    enum class IconStylePref(val key: String) {
        FLAT_SKETCH(KEY_ICON_STYLE_FLAT_SKETCH),
        TWINKLE_SHADOW(KEY_ICON_STYLE_TWINKLE_SHADOW)
    }

    enum class WidgetStylePref(val key: String) {
        CLASSIC(KEY_WIDGET_STYLE_CLASSIC),
        SOLID(KEY_WIDGET_STYLE_SOLID)
    }

    enum class TempUnitPref(val key: String) {
        AUTO(KEY_TEMP_AUTO),
        CELSIUS(KEY_TEMP_CELSIUS),
        FAHRENHEIT(KEY_TEMP_FAHRENHEIT)
    }

    enum class ColorPref(val key: String) {
        AUTO(KEY_COLOR_AUTO),
        LIGHT(KEY_COLOR_LIGHT),
        DARK(KEY_COLOR_DARK)
    }

    enum class AuxDisplayPref(val key: String) {
        NOTHING(KEY_DISPLAY_NOTHING),
        UPDATE_TIME(KEY_DISPLAY_UPDATE_TIME),
    }

    fun getWidgetPreferences(context: Context): WidgetPreferences {
        val textColorPref = getTextColorPreference(context)
        val iconColorPref = getIconColorPreference(context)
        val iconStylePref = getIconStylePreference(context)
        val widgetStylePref = getWidgetStylePreference(context)
        val tempUnitPref = getTempUnitPreference(context)
        val auxDisplayPref = getAuxDisplayPreference(context)
        val weatherApp = getWeatherApp(context)

        val textColor = when (textColorPref) {
            ColorPref.AUTO -> when (widgetStylePref) {
                WidgetStylePref.CLASSIC -> getTextColorForWallpaper(context)
                WidgetStylePref.SOLID -> TextColor.THEMED
            }
            ColorPref.DARK -> TextColor.DARK
            ColorPref.LIGHT -> TextColor.LIGHT
        }
        val iconColor = when (iconColorPref) {
            ColorPref.LIGHT -> IconColor.LIGHT
            ColorPref.DARK -> IconColor.DARK
            ColorPref.AUTO -> when (textColor) {
                TextColor.LIGHT -> IconColor.LIGHT
                TextColor.DARK -> IconColor.DARK
                TextColor.THEMED -> IconColor.THEMED
            }
        }
        val widgetStyle = when (widgetStylePref) {
            WidgetStylePref.SOLID -> WidgetStyle.SOLID
            WidgetStylePref.CLASSIC ->
                if (textColor == TextColor.LIGHT && iconStylePref == IconStylePref.TWINKLE_SHADOW)
                    WidgetStyle.SHADOW else WidgetStyle.DEFAULT
        }
        val iconStyle = when (iconStylePref) {
            IconStylePref.TWINKLE_SHADOW -> IconStyle.TWINKLE_SHADOW
            IconStylePref.FLAT_SKETCH -> IconStyle.FLAT_SKETCH
        }
        val tempUnit = when (tempUnitPref) {
            TempUnitPref.AUTO -> {
                if (LocalePreferences.getTemperatureUnit()
                    == LocalePreferences.TemperatureUnit.CELSIUS) {
                    TempUnit.CELSIUS
                } else {
                    TempUnit.FAHRENHEIT
                }
            }
            TempUnitPref.CELSIUS -> TempUnit.CELSIUS
            TempUnitPref.FAHRENHEIT -> TempUnit.FAHRENHEIT
        }
        val auxDisplay = when (auxDisplayPref) {
            AuxDisplayPref.NOTHING -> AuxDisplay.NOTHING
            AuxDisplayPref.UPDATE_TIME -> AuxDisplay.UPDATE_TIME
        }

        return WidgetPreferences(
            showAlarms = getAlarmPreference(context),
            showWeather = getWeatherPreference(context),
            showBirthdays = getBirthdayPreference(context),
            useLocationFallback = getLocationFallbackPreference(context),
            showDailyForecast = getDailyForecastPreference(context),
            showWeatherWarning = getWeatherWarningPreference(context),
            permanentAlarm = getPermanentAlarmPreference(context),
            tempUnit = tempUnit,
            iconStyle = iconStyle,
            widgetStyle = widgetStyle,
            iconColor = iconColor,
            textColor = textColor,
            auxDisplay = auxDisplay,
            weatherApp = weatherApp
        )
    }

    fun setWeatherPreference(context: Context, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(KEY_WEATHER_SWITCH, value)
        }
    }

    fun setTempUnitPreference(context: Context, pref: TempUnitPref) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(KEY_TEMP_UNIT_LIST, pref.key)
        }
    }

    fun setTextColorPreference(context: Context, pref: ColorPref) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(KEY_TEXT_COLOR_LIST, pref.key)
        }
    }

    fun setIconColorPreference(context: Context, pref: ColorPref) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(KEY_ICON_COLOR_LIST, pref.key)
        }
    }

    fun setWeatherApp(context: Context, weatherApp: WeatherApp) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(KEY_WEATHER_APP_LIST, weatherApp.key)
        }
    }

    fun setAlarmPreference(context: Context, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(KEY_ALARM_SWITCH, value)
        }
    }

    fun getWidgetStylePreference(context: Context): WidgetStylePref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_WIDGET_STYLE_LIST, null)
        return WidgetStylePref.entries.find { it.key == key } ?: WidgetStylePref.CLASSIC
    }

    private fun getWeatherPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_WEATHER_SWITCH, false)
    }

    private fun getBirthdayPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_BIRTHDAY_SWITCH, false)
    }

    private fun getDailyForecastPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_DAILY_FORECAST, true)
    }

    private fun getWeatherWarningPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_WEATHER_WARNING, true)
    }

    private fun getAlarmPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_ALARM_SWITCH, false)
    }

    private fun getTextColorPreference(context: Context): ColorPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_TEXT_COLOR_LIST, null)
        return ColorPref.entries.find { it.key == key } ?: ColorPref.AUTO
    }

    private fun getIconColorPreference(context: Context): ColorPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_ICON_COLOR_LIST, null)
        return ColorPref.entries.find { it.key == key } ?: ColorPref.AUTO
    }

    private fun getIconStylePreference(context: Context): IconStylePref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_ICON_STYLE_LIST, null)
        return IconStylePref.entries.find { it.key == key } ?: IconStylePref.FLAT_SKETCH

    }

    private fun getTempUnitPreference(context: Context): TempUnitPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_TEMP_UNIT_LIST, null)
        return TempUnitPref.entries.find { it.key == key } ?: TempUnitPref.AUTO
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

    private fun getPermanentAlarmPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_PERMANENT_ALARM, false)
    }

    private fun getWeatherApp(context: Context): WeatherApp {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_WEATHER_APP_LIST, null)
        return WeatherApp.entries.find { it.key == key } ?: WeatherApp.NONE
    }

    private fun getTextColorForWallpaper(context: Context): TextColor {
        val needsLightText = WallpaperManager
            .getInstance(context)
            .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            ?.colorHints
            ?.let { it and WallpaperColors.HINT_SUPPORTS_DARK_TEXT == 0 } ?: true
        return if (needsLightText) TextColor.LIGHT else TextColor.DARK
    }
}
