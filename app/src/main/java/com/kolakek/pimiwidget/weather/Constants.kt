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

internal const val BASE_URL = "https://api.open-meteo.com/v1/forecast"

internal const val LATITUDE_KEY = "latitude"
internal const val LONGITUDE_KEY = "longitude"

internal const val MINUTELY_KEY = "minutely_15"
internal const val MINUTELY_VALUE =
    "temperature_2m,weather_code,is_day,cloud_cover,cape,precipitation_probability,visibility"

internal const val HOURLY_KEY = "hourly"
internal const val HOURLY_VALUE = "temperature_2m,weather_code,is_day,uv_index," +
        "uv_index_clear_sky,cloud_cover,apparent_temperature"

internal const val DAILY_KEY = "daily"
internal const val DAILY_VALUE =
    "weather_code,temperature_2m_max,temperature_2m_min,cloud_cover_mean,cape_max," +
            "precipitation_probability_max,visibility_mean"

internal const val FORECAST_MINUTES_KEY = "forecast_minutely_15"
internal const val FORECAST_MINUTES_VALUE = "12"

internal const val FORECAST_HOURS_KEY = "forecast_hours"
internal const val FORECAST_HOURS_VALUE = "4"

internal const val FORECAST_DAYS_KEY = "forecast_days"
internal const val FORECAST_DAYS_VALUE = "2"

internal const val TIMEFORMAT_KEY = "timeformat"
internal const val TIMEFORMAT_VALUE = "unixtime"

internal const val TIMEZONE_KEY = "timezone"
internal const val TIMEZONE_VALUE = "auto"

internal const val MIN_CLOUD_COVER_OVERCAST = 90
internal const val MIN_CLOUD_COVER_MOSTLY_CLOUDY = 70
internal const val MIN_CLOUD_COVER_PARTLY_CLOUDY = 40
internal const val MIN_CLOUD_COVER_MAINLY_CLEAR = 20
internal const val MIN_CAPE_THUNDERSTORM = 500
internal const val MIN_POP_THUNDERSTORM = 40
internal const val MAX_VISIBILITY_FOG = 1000
internal const val MIN_PROBABILITY_PRECIP = 30

internal const val CODE_LIGHT_SHOWERS = 237
internal const val CODE_NO_PRECIP = 1138

internal const val EXTR_UV_MIN_UV_INDEX = 11
internal const val EXTR_UV_MAX_CLOUD_COVER = 50
internal const val SEVR_UV_MIN_UV_INDEX = 8
internal const val SEVR_UV_MAX_CLOUD_COVER = 70
internal const val SEVR_HEAT_MIN_APPARENT_TEMP = 40.6
internal const val EXTR_HEAT_MIN_APPARENT_TEMP = 46.1