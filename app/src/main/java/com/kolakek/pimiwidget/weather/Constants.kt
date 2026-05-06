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
internal const val MINUTELY_VALUE = "temperature_2m,weather_code,is_day"

internal const val HOURLY_KEY = "hourly"
internal const val HOURLY_VALUE = "temperature_2m,weather_code,is_day"

internal const val DAILY_KEY = "daily"
internal const val DAILY_VALUE = "weather_code,temperature_2m_max,temperature_2m_min,rain_sum,showers_sum,snowfall_sum,visibility_mean,cloud_cover_mean"

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
