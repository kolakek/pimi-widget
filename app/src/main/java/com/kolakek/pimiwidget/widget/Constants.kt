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

import android.Manifest

internal const val REQUIRED_PERMISSION = Manifest.permission.ACCESS_BACKGROUND_LOCATION

internal const val KEY_TEMP_UNITS = "temp_list"
internal const val KEY_CELSIUS = "temp_unit_celsius"
internal const val KEY_FAHRENHEIT = "temp_unit_fahrenheit"
internal const val KEY_TEXT_COLOR_LIST = "text_color_list"
internal const val KEY_ICON_STYLE_LIST = "icon_style_list"
internal const val KEY_COLOR_LIGHT = "color_light"
internal const val KEY_COLOR_AUTO = "color_auto"
internal const val KEY_ICON_STYLE_OUTLINED = "icon_style_outlined"
internal const val KEY_ICON_STYLE_FILLED = "icon_style_filled"
internal const val KEY_DEBUG_INFO = "debug_info"
internal const val KEY_SHARED_DATA = "shared_data"
internal const val KEY_WEATHER_SWITCH = "weather_switch"
internal const val KEY_SOURCE_CODE = "source_code"
internal const val KEY_DAILY_FORECAST = "daily_forecast"

internal const val SOURCE_CODE_URL = "https://github.com/kolakek/pimi-widget"
internal const val LOCATION_URL = "https://www.openstreetmap.org"
internal const val LOCATION_URL_ZOOM = 13

internal const val ALT_WEATHER_APP = "com.google.android.apps.weather"
internal const val WORKER_INIT_DELAY_MILLIS = 2 * 60 * 1000L

internal const val FORECAST_TODAY_HOUR_ON = 6
internal const val FORECAST_TODAY_HOUR_OFF = 10
internal const val FORECAST_TOMORROW_HOUR_ON = 20
internal const val FORECAST_TOMORROW_HOUR_OFF = 24

internal const val FLURRIES_CM = 0.1
internal const val SNOW_CM = 1.0
internal const val HEAVY_SNOW_CM = 5.0
internal const val DRIZZLE_MM = 0.1
internal const val RAIN_MM = 2.5
internal const val HEAVY_RAIN_MM = 10.0
internal const val FOG_VISIBILITY_M = 5000.0
internal const val CLOUDY_PERCENT = 90
internal const val MOSTLY_CLOUDY_PERCENT = 65
internal const val PARTLY_CLOUDY_PERCENT = 35
internal const val MOSTLY_CLEAR_PERCENT = 10
