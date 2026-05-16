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

package com.kolakek.pimiwidget.resources

import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.weather.WeatherCode

object WeatherString {

    fun getShortWeatherStrId(weatherCode: WeatherCode, isDay: Boolean): Int {
        return when (weatherCode) {
            WeatherCode.CLEAR_SKY ->
                if (isDay) R.string.clear_sky_day else R.string.clear_sky_night

            WeatherCode.MAINLY_CLEAR ->
                if (isDay) R.string.mainly_clear_day else R.string.mainly_clear_night

            WeatherCode.PARTLY_CLOUDY ->
                if (isDay) R.string.partly_cloudy_day else R.string.partly_cloudy_night

            WeatherCode.MOSTLY_CLOUDY -> R.string.mostly_cloudy

            WeatherCode.OVERCAST -> R.string.overcast

            WeatherCode.FOG -> R.string.fog

            WeatherCode.DRIZZLE,
            WeatherCode.DRIZZLE_AND_SKY -> R.string.drizzle

            WeatherCode.LIGHT_RAIN,
            WeatherCode.LIGHT_RAIN_AND_SKY -> R.string.light_rain

            WeatherCode.LIGHT_RAIN_SHOWERS,
            WeatherCode.LIGHT_RAIN_SHOWERS_AND_SKY -> R.string.light_rain_showers

            WeatherCode.RAIN,
            WeatherCode.RAIN_AND_SKY -> R.string.rain

            WeatherCode.RAIN_SHOWERS,
            WeatherCode.RAIN_SHOWERS_AND_SKY -> R.string.rain_showers

            WeatherCode.HEAVY_RAIN,
            WeatherCode.HEAVY_RAIN_AND_SKY -> R.string.heavy_rain

            WeatherCode.HEAVY_RAIN_SHOWERS,
            WeatherCode.HEAVY_RAIN_SHOWERS_AND_SKY -> R.string.heavy_rain_showers

            WeatherCode.LIGHT_SNOW,
            WeatherCode.LIGHT_SNOW_AND_SKY -> R.string.light_snow

            WeatherCode.LIGHT_SNOW_SHOWERS,
            WeatherCode.LIGHT_SNOW_SHOWERS_AND_SKY -> R.string.light_snow_showers

            WeatherCode.SNOW,
            WeatherCode.SNOW_AND_SKY -> R.string.snow

            WeatherCode.HEAVY_SNOW,
            WeatherCode.HEAVY_SNOW_AND_SKY -> R.string.heavy_snow

            WeatherCode.HEAVY_SNOW_SHOWERS,
            WeatherCode.HEAVY_SNOW_SHOWERS_AND_SKY -> R.string.heavy_snow_showers

            WeatherCode.SNOW_GRAINS -> R.string.snow_grains

            WeatherCode.FREEZING_DRIZZLE -> R.string.freezing_drizzle

            WeatherCode.LIGHT_FREEZING_RAIN -> R.string.light_freezing_rain

            WeatherCode.FREEZING_RAIN -> R.string.freezing_rain

            WeatherCode.POTENTIAL_THUNDERSTORM_AND_SKY,
            WeatherCode.POTENTIAL_THUNDERSTORM,
            WeatherCode.POTENTIAL_THUNDERSTORM_AND_RAIN -> R.string.potential_thunderstorms

            WeatherCode.THUNDERSTORM_AND_SKY,
            WeatherCode.THUNDERSTORM -> R.string.thunderstorm

            WeatherCode.HEAVY_THUNDERSTORM -> R.string.heavy_thunderstorms
        }
    }
}
