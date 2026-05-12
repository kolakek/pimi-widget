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

object WeatherStrings {

    fun getShortWeatherStrId(
        wmoCode: Int,
        isDay: Int,
        cloudCover: Int,
    ): Int? {
        val weatherCode = WeatherCodeMapper.mapWmoCode(
            wmoCode,
            isDay == 1,
            cloudCover) ?: return null
        return getShortStrId(weatherCode)
    }

    private fun getShortStrId(weatherCode: WeatherCodes): Int {
        return when (weatherCode) {
            WeatherCodes.CLEAR_SKY_DAY,
            WeatherCodes.CLEAR_SKY_NIGHT -> R.string.clear

            WeatherCodes.MAINLY_CLEAR_DAY,
            WeatherCodes.MAINLY_CLEAR_NIGHT -> R.string.mostly_clear

            WeatherCodes.PARTLY_CLOUDY_DAY,
            WeatherCodes.PARTLY_CLOUDY_NIGHT -> R.string.partly_cloudy

            WeatherCodes.CLOUDY -> R.string.cloudy

            WeatherCodes.FOG -> R.string.foggy

            WeatherCodes.DRIZZLE,
            WeatherCodes.DRIZZLE_AND_SKY_DAY,
            WeatherCodes.DRIZZLE_AND_SKY_NIGHT -> R.string.drizzle

            WeatherCodes.LIGHT_RAIN,
            WeatherCodes.LIGHT_RAIN_AND_SKY_DAY,
            WeatherCodes.LIGHT_RAIN_AND_SKY_NIGHT -> R.string.clear // ToDo

            WeatherCodes.LIGHT_RAIN_SHOWERS,
            WeatherCodes.LIGHT_RAIN_SHOWERS_AND_SKY_DAY,
            WeatherCodes.LIGHT_RAIN_SHOWERS_AND_SKY_NIGHT -> R.string.clear // ToDo

            WeatherCodes.RAIN,
            WeatherCodes.RAIN_AND_SKY_DAY,
            WeatherCodes.RAIN_AND_SKY_NIGHT -> R.string.clear // ToDo

            WeatherCodes.RAIN_SHOWERS,
            WeatherCodes.RAIN_SHOWERS_AND_SKY_DAY,
            WeatherCodes.RAIN_SHOWERS_AND_SKY_NIGHT -> R.string.rain_showers

            WeatherCodes.HEAVY_RAIN,
            WeatherCodes.HEAVY_RAIN_AND_SKY_DAY,
            WeatherCodes.HEAVY_RAIN_AND_SKY_NIGHT -> R.string.heavy_rain

            WeatherCodes.HEAVY_RAIN_SHOWERS,
            WeatherCodes.HEAVY_RAIN_SHOWERS_AND_SKY_DAY,
            WeatherCodes.HEAVY_RAIN_SHOWERS_AND_SKY_NIGHT -> R.string.clear // ToDo

            WeatherCodes.LIGHT_SNOW,
            WeatherCodes.LIGHT_SNOW_AND_SKY_DAY,
            WeatherCodes.LIGHT_SNOW_AND_SKY_NIGHT -> R.string.clear // ToDo

            WeatherCodes.LIGHT_SNOW_SHOWERS,
            WeatherCodes.LIGHT_SNOW_SHOWERS_AND_SKY_DAY,
            WeatherCodes.LIGHT_SNOW_SHOWERS_AND_SKY_NIGHT -> R.string.clear // ToDo

            WeatherCodes.SNOW,
            WeatherCodes.SNOW_AND_SKY_DAY,
            WeatherCodes.SNOW_AND_SKY_NIGHT -> R.string.clear // ToDo

            WeatherCodes.HEAVY_SNOW,
            WeatherCodes.HEAVY_SNOW_AND_SKY_DAY,
            WeatherCodes.HEAVY_SNOW_AND_SKY_NIGHT -> R.string.heavy_snow

            WeatherCodes.HEAVY_SNOW_SHOWERS,
            WeatherCodes.HEAVY_SNOW_SHOWERS_AND_SKY_DAY,
            WeatherCodes.HEAVY_SNOW_SHOWERS_AND_SKY_NIGHT -> R.string.clear // ToDo

            WeatherCodes.SNOW_GRAINS -> R.string.clear // ToDo

            WeatherCodes.FREEZING_DRIZZLE -> R.string.freezing_drizzle

            WeatherCodes.LIGHT_FREEZING_RAIN -> R.string.clear // ToDo

            WeatherCodes.FREEZING_RAIN -> R.string.freezing_rain

            WeatherCodes.THUNDERSTORM,
            WeatherCodes.THUNDERSTORM_AND_SKY_DAY,
            WeatherCodes.THUNDERSTORM_AND_SKY_NIGHT -> R.string.thunderstorms
        }
    }
}
