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
import android.content.res.Configuration
import androidx.core.content.edit
import androidx.core.text.util.LocalePreferences
import androidx.preference.PreferenceManager
import com.kolakek.pimiwidget.location.FIXED_LOCATION_NAME
import com.kolakek.pimiwidget.location.LocationData
import com.kolakek.pimiwidget.location.LocationFormat
import com.kolakek.pimiwidget.utility.WeatherApp

object PreferencesHelper {

    enum class AppIconStylePref(val key: String) {
        FLAT_SKETCH(KEY_APP_ICON_STYLE_FLAT_SKETCH),
        TWINKLE_SHADOW(KEY_APP_ICON_STYLE_TWINKLE_SHADOW),
        FOLLOW_WIDGET(KEY_APP_ICON_STYLE_WIDGET)
    }

    enum class WidgetIconStylePref(val key: String) {
        FLAT_SKETCH(KEY_WIDGET_ICON_STYLE_FLAT_SKETCH),
        TWINKLE_SHADOW(KEY_WIDGET_ICON_STYLE_TWINKLE_SHADOW)
    }

    enum class WidgetStylePref(val key: String) {
        CLASSIC(KEY_WIDGET_STYLE_CLASSIC),
        MODERN(KEY_WIDGET_STYLE_MODERN),
        SOLID(KEY_WIDGET_STYLE_SOLID)
    }

    enum class TempUnitPref(val key: String) {
        AUTO(KEY_TEMP_UNIT_AUTO),
        CELSIUS(KEY_TEMP_UNIT_CELSIUS),
        FAHRENHEIT(KEY_TEMP_UNIT_FAHRENHEIT)
    }

    enum class RainUnitPref(val key: String) {
        AUTO(KEY_RAIN_UNIT_AUTO),
        MM(KEY_RAIN_UNIT_MM),
        INCH(KEY_RAIN_UNIT_INCH)
    }

    enum class SnowUnitPref(val key: String) {
        AUTO(KEY_SNOW_UNIT_AUTO),
        CM(KEY_SNOW_UNIT_CM),
        INCH(KEY_SNOW_UNIT_INCH)
    }

    enum class PressureUnitPref(val key: String) {
        AUTO(KEY_PRESSURE_UNIT_AUTO),
        HPA(KEY_PRESSURE_UNIT_HPA),
        MB(KEY_PRESSURE_UNIT_MB),
        MMHG(KEY_PRESSURE_UNIT_MMHG),
        INHG(KEY_PRESSURE_UNIT_INHG)
    }

    enum class WindUnitPref(val key: String) {
        AUTO(KEY_WIND_UNIT_AUTO),
        KMH(KEY_WIND_UNIT_KMH),
        MS(KEY_WIND_UNIT_MS),
        MPH(KEY_WIND_UNIT_MPH),
        KNOT(KEY_WIND_UNIT_KNOT),
    }

    enum class ColorPref(val key: String) {
        AUTO(KEY_COLOR_AUTO),
        LIGHT(KEY_COLOR_LIGHT),
        DARK(KEY_COLOR_DARK)
    }

    enum class AuxDisplayPref(val key: String) {
        NOTHING(KEY_DISPLAY_NOTHING),
        UPDATE_TIME(KEY_DISPLAY_UPDATE_TIME),
        PLACE_CONDITION(KEY_DISPLAY_PLACE_CONDITION)
    }

    fun getAppPreferences(context: Context): AppPreferences {
        val iconStylePref = getAppIconStylePreference(context)
        val tempUnitPref = getTempUnitPreference(context)
        val rainUnitPref = getRainUnitPreference(context)
        val snowUnitPref = getSnowUnitPreference(context)
        val windUnitPref = getWindUnitPreference(context)
        val pressureUnitPref = getPressureUnitPreference(context)

        val iconStyle = when (iconStylePref) {
            AppIconStylePref.TWINKLE_SHADOW -> IconStyle.TWINKLE_SHADOW
            AppIconStylePref.FLAT_SKETCH -> IconStyle.FLAT_SKETCH
            AppIconStylePref.FOLLOW_WIDGET -> {
                when (getWidgetIconStylePreference(context)) {
                    WidgetIconStylePref.FLAT_SKETCH -> IconStyle.FLAT_SKETCH
                    WidgetIconStylePref.TWINKLE_SHADOW -> IconStyle.TWINKLE_SHADOW
                }
            }
        }
        val iconColor = if (context.isNightMode()) IconColor.LIGHT else IconColor.DARK

        return AppPreferences(
            iconStyle = iconStyle,
            iconColor = iconColor,
            tempUnit = tempUnitFromPref(tempUnitPref),
            windUnit = windUnitFromPref(windUnitPref),
            pressureUnit = pressureUnitFromPref(pressureUnitPref),
            rainUnit = rainUnitFromPref(rainUnitPref),
            snowUnit = snowUnitFromPref(snowUnitPref),
            showWeather = getWeatherPreference(context),
        )
    }

    fun getWidgetPreferences(context: Context): WidgetPreferences {
        val textColorPref = getTextColorPreference(context)
        val iconColorPref = getIconColorPreference(context)
        val iconStylePref = getWidgetIconStylePreference(context)
        val widgetStylePref = getWidgetStylePreference(context)
        val tempUnitPref = getTempUnitPreference(context)
        val auxDisplayPref = getAuxDisplayPreference(context)
        val weatherApp = getWeatherApp(context)

        val textColor = when (textColorPref) {
            ColorPref.AUTO -> when (widgetStylePref) {
                WidgetStylePref.CLASSIC,
                WidgetStylePref.MODERN
                    -> getTextColorForWallpaper(context)
                WidgetStylePref.SOLID -> TextColor.DYNAMIC
            }

            ColorPref.DARK -> TextColor.DARK
            ColorPref.LIGHT -> TextColor.LIGHT
        }
        val iconColor = when (iconColorPref) {
            ColorPref.LIGHT -> IconColor.LIGHT
            ColorPref.DARK -> IconColor.DARK
            ColorPref.AUTO -> when (widgetStylePref) {
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
        return WidgetPreferences(
            showAlarms = getAlarmPreference(context),
            showWeather = getWeatherPreference(context),
            showBirthdays = getBirthdayPreference(context),
            useLocationFallback = getLocationFallbackPreference(context),
            showDailyForecast = getDailyForecastPreference(context),
            showWeatherWarning = getWeatherWarningPreference(context),
            permanentAlarm = getPermanentAlarmPreference(context),
            showTempUnit = getShowTempUnitPreference(context),
            tempUnit = tempUnitFromPref(tempUnitPref),
            iconStyle = iconStyle,
            widgetStyle = widgetStyle,
            iconColor = iconColor,
            textColor = textColor,
            auxDisplay = auxDisplay,
            weatherApp = weatherApp,
            fixedLocation = getFixedLocation(context)
        )
    }

    /**
     * Returns the location chosen by the user, or null when the device location is used.
     * The returned time is the time of the call, as a fixed location does not age.
     */
    fun getFixedLocation(context: Context): LocationData? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val lat = prefs.getString(KEY_FIXED_LOCATION_LAT, null)?.toDoubleOrNull()
        val long = prefs.getString(KEY_FIXED_LOCATION_LONG, null)?.toDoubleOrNull()
        if (lat == null || long == null) return null

        val place = prefs.getString(KEY_FIXED_LOCATION_NAME, null)
            ?.takeIf { it.isNotBlank() }
            ?: LocationFormat.coordinatesLabel(lat, long)

        return LocationData(
            timeMillis = System.currentTimeMillis(),
            lat = lat,
            long = long,
            place = place,
            locationType = FIXED_LOCATION_NAME
        )
    }

    fun setFixedLocation(context: Context, lat: Double, long: Double, place: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(KEY_FIXED_LOCATION_LAT, lat.toString())
            putString(KEY_FIXED_LOCATION_LONG, long.toString())
            putString(KEY_FIXED_LOCATION_NAME, place)
        }
    }

    fun clearFixedLocation(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            remove(KEY_FIXED_LOCATION_LAT)
            remove(KEY_FIXED_LOCATION_LONG)
            remove(KEY_FIXED_LOCATION_NAME)
        }
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

    fun setWidgetStylePreference(context: Context, pref: WidgetStylePref) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(KEY_WIDGET_STYLE_LIST, pref.key)
        }
    }

    fun setAuxDisplayPreference(context: Context, pref: AuxDisplayPref) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(KEY_AUX_DISPLAY_LIST, pref.key)
        }
    }

    fun setLocationFallbackPreference(context: Context, pref: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(KEY_LOCATION_FALLBACK, pref)
        }
    }

    fun getWidgetStylePreference(context: Context): WidgetStylePref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_WIDGET_STYLE_LIST, null)
        return WidgetStylePref.entries.find { it.key == key } ?: WidgetStylePref.SOLID
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

    private fun getWidgetIconStylePreference(context: Context): WidgetIconStylePref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_WIDGET_ICON_STYLE_LIST, null)
        return WidgetIconStylePref.entries.find { it.key == key }
            ?: WidgetIconStylePref.TWINKLE_SHADOW

    }

    private fun getAppIconStylePreference(context: Context): AppIconStylePref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_APP_ICON_STYLE_LIST, null)
        return AppIconStylePref.entries.find { it.key == key }
            ?: AppIconStylePref.FLAT_SKETCH

    }

    private fun getTempUnitPreference(context: Context): TempUnitPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_TEMP_UNIT_LIST, null)
        return TempUnitPref.entries.find { it.key == key } ?: TempUnitPref.AUTO
    }

    private fun getRainUnitPreference(context: Context): RainUnitPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_RAIN_UNIT_LIST, null)
        return RainUnitPref.entries.find { it.key == key } ?: RainUnitPref.AUTO
    }

    private fun getSnowUnitPreference(context: Context): SnowUnitPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_SNOW_UNIT_LIST, null)
        return SnowUnitPref.entries.find { it.key == key } ?: SnowUnitPref.AUTO
    }

    private fun getPressureUnitPreference(context: Context): PressureUnitPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_PRESSURE_UNIT_LIST, null)
        return PressureUnitPref.entries.find { it.key == key } ?: PressureUnitPref.AUTO
    }

    private fun getWindUnitPreference(context: Context): WindUnitPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_WIND_UNIT_LIST, null)
        return WindUnitPref.entries.find { it.key == key } ?: WindUnitPref.AUTO
    }

    private fun getAuxDisplayPreference(context: Context): AuxDisplayPref {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_AUX_DISPLAY_LIST, null)
        return AuxDisplayPref.entries.find { it.key == key } ?: AuxDisplayPref.PLACE_CONDITION
    }

    private fun getLocationFallbackPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_LOCATION_FALLBACK, true)
    }

    private fun getPermanentAlarmPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_PERMANENT_ALARM, false)
    }

    private fun getShowTempUnitPreference(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_SHOW_TEMP_UNIT, true)
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
