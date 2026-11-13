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

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import androidx.core.text.util.LocalePreferences
import androidx.preference.PreferenceManager
import com.kolakek.pimiwidget.location.LocationSource
import com.kolakek.pimiwidget.ui.AppSettings
import com.kolakek.pimiwidget.widget.WidgetSettings
import com.kolakek.pimiwidget.utility.WeatherApp

object SettingsRepository {

    fun loadAppSettings(context: Context): AppSettings {
        val iconStylePref = getAppIconStylePref(context)
        val tempUnitPref = getTempUnitPref(context)
        val rainUnitPref = getRainUnitPref(context)
        val snowUnitPref = getSnowUnitPref(context)
        val windUnitPref = getWindUnitPref(context)
        val pressureUnitPref = getPressureUnitPref(context)

        val iconStyle = when (iconStylePref) {
            AppIconStylePref.TWINKLE_SHADOW -> IconStyle.TWINKLE_SHADOW
            AppIconStylePref.FLAT_SKETCH -> IconStyle.FLAT_SKETCH
            AppIconStylePref.FOLLOW_WIDGET -> {
                when (getWidgetIconStylePref(context)) {
                    WidgetIconStylePref.FLAT_SKETCH -> IconStyle.FLAT_SKETCH
                    WidgetIconStylePref.TWINKLE_SHADOW -> IconStyle.TWINKLE_SHADOW
                }
            }
        }
        val iconColor = if (context.isNightMode()) IconColor.LIGHT else IconColor.DARK

        return AppSettings(
            iconStyle = iconStyle,
            iconColor = iconColor,
            tempUnit = tempUnitFromPref(tempUnitPref),
            windUnit = windUnitFromPref(windUnitPref),
            pressureUnit = pressureUnitFromPref(pressureUnitPref),
            rainUnit = rainUnitFromPref(rainUnitPref),
            snowUnit = snowUnitFromPref(snowUnitPref),
            showWeather = isWeatherEnabled(context),
        )
    }

    fun loadWidgetSettings(context: Context): WidgetSettings {
        val widgetColorPref = getWidgetColorPref(context)
        val textColorPref = getTextColorPref(context)
        val iconColorPref = getIconColorPref(context)
        val iconStylePref = getWidgetIconStylePref(context)
        val widgetStylePref = getWidgetStylePref(context)
        val tempUnitPref = getTempUnitPref(context)
        val auxDisplayPref = getAuxDisplayPref(context)
        val weatherLocationPref = getLocationSourcePref(context)
        val weatherApp = getWeatherApp(context)

        val widgetColor = when (widgetColorPref) {
            WidgetColorPref.DYNAMIC -> WidgetColor.DYNAMIC
            WidgetColorPref.SYSTEM -> WidgetColor.SYSTEM
            WidgetColorPref.LIGHT -> WidgetColor.LIGHT
            WidgetColorPref.DARK -> WidgetColor.DARK
        }
        val textColor = when (textColorPref) {
            TextColorPref.AUTO -> when (widgetStylePref) {
                WidgetStylePref.CLASSIC,
                WidgetStylePref.MODERN
                    -> getTextColorForWallpaper(context)
                WidgetStylePref.SOLID -> TextColor.DYNAMIC
            }

            TextColorPref.DARK -> TextColor.DARK
            TextColorPref.LIGHT -> TextColor.LIGHT
        }
        val iconColor = when (iconColorPref) {
            IconColorPref.LIGHT -> IconColor.LIGHT
            IconColorPref.DARK -> IconColor.DARK
            IconColorPref.AUTO -> when (widgetStylePref) {
                WidgetStylePref.SOLID -> IconColor.LIGHT
                else -> if (textColor == TextColor.DARK) IconColor.DARK else IconColor.LIGHT
            }
        }
        val needsShadowStyle =
            (textColor == TextColor.LIGHT) && (iconStylePref == WidgetIconStylePref.TWINKLE_SHADOW)

        val widgetStyle = when (widgetStylePref) {
            WidgetStylePref.SOLID -> WidgetStyle.SOLID
            WidgetStylePref.CLASSIC ->
                if (needsShadowStyle) WidgetStyle.CLASSIC_SHADOW else WidgetStyle.CLASSIC
            WidgetStylePref.MODERN ->
                if (needsShadowStyle) WidgetStyle.MODERN_SHADOW else WidgetStyle.MODERN
        }
        val iconStyle = when (iconStylePref) {
            WidgetIconStylePref.TWINKLE_SHADOW -> IconStyle.TWINKLE_SHADOW
            WidgetIconStylePref.FLAT_SKETCH -> IconStyle.FLAT_SKETCH
        }
        val auxDisplay = when (auxDisplayPref) {
            AuxDisplayPref.NOTHING -> AuxDisplay.NOTHING
            AuxDisplayPref.PLACE_CONDITION -> AuxDisplay.PLACE_CONDITION
            AuxDisplayPref.UPDATE_TIME -> AuxDisplay.UPDATE_TIME
        }
        val locationSource = when (weatherLocationPref) {
            LocationSourcePref.DEVICE -> LocationSource.DEVICE
            LocationSourcePref.FIXED -> LocationSource.FIXED
        }
        return WidgetSettings(
            showAlarms = isAlarmEnabled(context),
            showWeather = isWeatherEnabled(context),
            showBirthday = isBirthdayEnabled(context),
            useLocationFallback = useLocationFallback(context),
            showDailyForecast = isDailyForecastEnabled(context),
            showWeatherWarning = isWeatherWarningEnabled(context),
            showPermanentAlarm = showPermanentAlarm(context),
            showTempUnit = showTempUnit(context),
            backgroundAlpha = getBackgroundAlpha(context),
            tempUnit = tempUnitFromPref(tempUnitPref),
            iconStyle = iconStyle,
            widgetStyle = widgetStyle,
            iconColor = iconColor,
            textColor = textColor,
            widgetColor = widgetColor,
            auxDisplay = auxDisplay,
            weatherApp = weatherApp,
            locationSource = locationSource
        )
    }

    fun isWidgetConfigured(context: Context, appWidgetId: Int): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("${KEY_WIDGET_CONFIGURED}_$appWidgetId", false)
    }

    fun setWidgetConfigured(context: Context, appWidgetId: Int, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean("${KEY_WIDGET_CONFIGURED}_$appWidgetId", value)
        }
    }

    fun removeWidgetConfigured(context: Context, appWidgetId: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            remove("${KEY_WIDGET_CONFIGURED}_$appWidgetId")
        }
    }

    fun setTempUnitPref(context: Context, pref: TempUnitPref) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(KEY_TEMP_UNIT_LIST, pref.key)
        }
    }

    fun setTextColorPref(context: Context, pref: TextColorPref) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(KEY_TEXT_COLOR_LIST, pref.key)
        }
    }

    fun setIconColorPref(context: Context, pref: IconColorPref) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(KEY_ICON_COLOR_LIST, pref.key)
        }
    }

    fun setWidgetStylePref(context: Context, pref: WidgetStylePref) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(KEY_WIDGET_STYLE_LIST, pref.key)
        }
    }

    fun setAuxDisplayPref(context: Context, pref: AuxDisplayPref) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(KEY_AUX_DISPLAY_LIST, pref.key)
        }
    }

    fun setLocationFallback(context: Context, pref: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(KEY_LOCATION_FALLBACK, pref)
        }
    }

    fun getWidgetStylePref(context: Context): WidgetStylePref =
        WidgetStylePref.fromKey(
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_WIDGET_STYLE_LIST, null)
        )

    private fun isWeatherEnabled(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_WEATHER_SWITCH, false)
    }

    private fun isBirthdayEnabled(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_BIRTHDAY_SWITCH, false)
    }

    private fun isDailyForecastEnabled(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_DAILY_FORECAST, true)
    }

    private fun isWeatherWarningEnabled(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_WEATHER_WARNING, true)
    }

    private fun isAlarmEnabled(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_ALARM_SWITCH, false)
    }

    fun getWidgetColorPref(context: Context): WidgetColorPref =
        WidgetColorPref.fromKey(
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_WIDGET_COLOR_LIST, null)
        )

    fun getTextColorPref(context: Context): TextColorPref =
        TextColorPref.fromKey(
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_TEXT_COLOR_LIST, null)
        )

    private fun getIconColorPref(context: Context): IconColorPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_ICON_COLOR_LIST, null)
        return IconColorPref.entries.find { it.key == key } ?: IconColorPref.AUTO
    }

    private fun getWidgetIconStylePref(context: Context): WidgetIconStylePref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_WIDGET_ICON_STYLE_LIST, null)
        return WidgetIconStylePref.entries.find { it.key == key }
            ?: WidgetIconStylePref.TWINKLE_SHADOW
    }

    private fun getAppIconStylePref(context: Context): AppIconStylePref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_APP_ICON_STYLE_LIST, null)
        return AppIconStylePref.entries.find { it.key == key }
            ?: AppIconStylePref.FLAT_SKETCH
    }

    private fun getTempUnitPref(context: Context): TempUnitPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_TEMP_UNIT_LIST, null)
        return TempUnitPref.entries.find { it.key == key } ?: TempUnitPref.AUTO
    }

    private fun getRainUnitPref(context: Context): RainUnitPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_RAIN_UNIT_LIST, null)
        return RainUnitPref.entries.find { it.key == key } ?: RainUnitPref.AUTO
    }

    private fun getSnowUnitPref(context: Context): SnowUnitPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_SNOW_UNIT_LIST, null)
        return SnowUnitPref.entries.find { it.key == key } ?: SnowUnitPref.AUTO
    }

    private fun getPressureUnitPref(context: Context): PressureUnitPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_PRESSURE_UNIT_LIST, null)
        return PressureUnitPref.entries.find { it.key == key } ?: PressureUnitPref.AUTO
    }

    private fun getWindUnitPref(context: Context): WindUnitPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_WIND_UNIT_LIST, null)
        return WindUnitPref.entries.find { it.key == key } ?: WindUnitPref.AUTO
    }

    private fun getAuxDisplayPref(context: Context): AuxDisplayPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_AUX_DISPLAY_LIST, null)
        return AuxDisplayPref.entries.find { it.key == key } ?: AuxDisplayPref.PLACE_CONDITION
    }

    private fun useLocationFallback(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_LOCATION_FALLBACK, true)
    }

    private fun showPermanentAlarm(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_PERMANENT_ALARM, false)
    }

    private fun showTempUnit(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_SHOW_TEMP_UNIT, true)
    }

    private fun getLocationSourcePref(context: Context): LocationSourcePref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_LOCATION_SOURCE_LIST, null)
        return LocationSourcePref.entries.find { it.key == key } ?: LocationSourcePref.DEVICE
    }

    private fun getBackgroundAlpha(context: Context): Int {
        val transparency = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(KEY_WIDGET_TRANSPARENCY, 0)
        return (100 - transparency) * 255 / 100
    }

    private fun getWeatherApp(context: Context): WeatherApp {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_WEATHER_APP_LIST, null)
        return WeatherApp.entries.find { it.key == key } ?: WeatherApp.PIMI
    }

    private fun getTextColorForWallpaper(context: Context): TextColor {
        val needsLightText = WallpaperManager
            .getInstance(context)
            .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            ?.colorHints
            ?.let { it and WallpaperColors.HINT_SUPPORTS_DARK_TEXT == 0 } ?: true
        return if (needsLightText) TextColor.LIGHT else TextColor.DARK
    }

    private fun tempUnitFromPref(tempUnitPref: TempUnitPref): TempUnit {
        return when (tempUnitPref) {
            TempUnitPref.AUTO -> {
                if (LocalePreferences.getTemperatureUnit()
                    == LocalePreferences.TemperatureUnit.CELSIUS
                ) {
                    TempUnit.CELSIUS
                } else {
                    TempUnit.FAHRENHEIT
                }
            }

            TempUnitPref.CELSIUS -> TempUnit.CELSIUS
            TempUnitPref.FAHRENHEIT -> TempUnit.FAHRENHEIT
        }
    }

    private fun windUnitFromPref(windUnitPref: WindUnitPref): WindUnit {
        return when (windUnitPref) {
            WindUnitPref.AUTO -> {
                when (UnitSystem.fromLocale()) {
                    UnitSystem.US, UnitSystem.UK -> WindUnit.MPH
                    UnitSystem.METRIC -> WindUnit.KMH
                }
            }

            WindUnitPref.KMH -> WindUnit.KMH
            WindUnitPref.MPH -> WindUnit.MPH
            WindUnitPref.MS -> WindUnit.MS
            WindUnitPref.KNOT -> WindUnit.KNOT
        }
    }

    private fun pressureUnitFromPref(pressureUnitPref: PressureUnitPref): PressureUnit {
        return when (pressureUnitPref) {
            PressureUnitPref.AUTO -> {
                when (UnitSystem.fromLocale()) {
                    UnitSystem.US -> PressureUnit.INHG
                    UnitSystem.UK -> PressureUnit.MB
                    UnitSystem.METRIC -> PressureUnit.HPA
                }
            }

            PressureUnitPref.HPA -> PressureUnit.HPA
            PressureUnitPref.MB -> PressureUnit.MB
            PressureUnitPref.INHG -> PressureUnit.INHG
            PressureUnitPref.MMHG -> PressureUnit.MMHG
        }
    }

    private fun rainUnitFromPref(rainUnitPref: RainUnitPref): RainUnit {
        return when (rainUnitPref) {
            RainUnitPref.AUTO -> {
                when (UnitSystem.fromLocale()) {
                    UnitSystem.US -> RainUnit.INCH
                    UnitSystem.UK -> RainUnit.MM
                    UnitSystem.METRIC -> RainUnit.MM
                }
            }

            RainUnitPref.MM -> RainUnit.MM
            RainUnitPref.INCH -> RainUnit.INCH
        }
    }

    private fun snowUnitFromPref(snowUnitPref: SnowUnitPref): SnowUnit {
        return when (snowUnitPref) {
            SnowUnitPref.AUTO -> {
                when (UnitSystem.fromLocale()) {
                    UnitSystem.US -> SnowUnit.INCH
                    UnitSystem.UK -> SnowUnit.CM
                    UnitSystem.METRIC -> SnowUnit.CM
                }
            }

            SnowUnitPref.CM -> SnowUnit.CM
            SnowUnitPref.INCH -> SnowUnit.INCH
        }
    }

    private fun Context.isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }
}
