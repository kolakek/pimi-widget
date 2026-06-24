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

package com.kolakek.pimiwidget.weather

internal const val CONNECT_TIMEOUT_MS = 10 * 1000L
internal const val REQUEST_TIMEOUT_MS = 30 * 1000L
internal const val SOCKET_TIMEOUT_MS = 30 * 1000L

internal const val BASE_URL = "https://api.open-meteo.com/v1/forecast"

internal const val LATITUDE_KEY = "latitude"
internal const val LONGITUDE_KEY = "longitude"

internal const val HOURLY_KEY = "hourly"
internal const val HOURLY_VALUE =
    "temperature_2m,weather_code,is_day,cloud_cover,uv_index,uv_index_clear_sky,visibility," +
            "apparent_temperature,rain,showers,precipitation_probability,wind_gusts_10m,cape"

internal const val DAILY_KEY = "daily"
internal const val DAILY_VALUE =
    "weather_code,temperature_2m_max,temperature_2m_min,cloud_cover_mean,cape_max," +
            "precipitation_probability_max,visibility_mean"

internal const val FORECAST_HOURS_KEY = "forecast_hours"
internal const val FORECAST_HOURS_VALUE = "7"

internal const val FORECAST_DAYS_KEY = "forecast_days"
internal const val FORECAST_DAYS_VALUE = "2"

internal const val TIMEFORMAT_KEY = "timeformat"
internal const val TIMEFORMAT_VALUE = "unixtime"

internal const val TIMEZONE_KEY = "timezone"
internal const val TIMEZONE_VALUE = "auto"

internal const val TEMP_UNIT_KEY = "temperature_unit"
internal const val TEMP_UNIT_VALUE = "celsius"

internal const val PRECIP_UNIT_KEY = "precipitation_unit"
internal const val PRECIP_UNIT_VALUE = "mm"

internal const val WIND_SPEED_UNIT_KEY = "wind_speed_unit"
internal const val WIND_SPEED_UNIT_VALUE = "kmh"

internal const val MAX_CLOUD_COVER_CLEAR_SKY = 20
internal const val MAX_CLOUD_COVER_MAINLY_CLEAR = 50
internal const val MAX_CLOUD_COVER_PARTLY_CLOUDY = 70
internal const val MIN_CLOUD_COVER_OVERCAST = 90

internal const val MIN_CAPE_THUNDERSTORM = 500
internal const val MIN_POP_THUNDERSTORM = 40
internal const val MAX_VISIBILITY_FOG = 1000
internal const val MIN_PROBABILITY_PRECIP = 30

internal const val CODE_LIGHT_SHOWERS = 237
internal const val CODE_NO_PRECIP = 1138

internal const val WARN_SVR_UV_MIN_UV_INDEX = 8
internal const val WARN_SVR_UV_MAX_CLOUD_COVER = 70
internal const val WARN_XTR_UV_MIN_UV_INDEX = 11
internal const val WARN_XTR_UV_MAX_CLOUD_COVER = 50
internal const val WARN_SVR_HEAT_MIN_APPARENT_TEMP_C = 38
internal const val WARN_XTR_HEAT_MIN_APPARENT_TEMP_C = 43
internal const val WARN_SVR_RAIN_MIN_MM = 25
internal const val WARN_SVR_RAIN_MIN_PROB = 70
internal const val WARN_XTR_RAIN_MIN_MM = 40
internal const val WARN_XTR_RAIN_MIN_PROB = 90
internal const val WARN_SVR_GUSTS_MIN_KMH = 105
internal const val WARN_XTR_GUSTS_MIN_KMH = 140
internal const val WARN_SVR_TSTORM_MIN_CAPE = 1500
internal const val WARN_XTR_TSTORM_MIN_CAPE = 2500
