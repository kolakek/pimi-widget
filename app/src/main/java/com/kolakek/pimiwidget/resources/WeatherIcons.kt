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

import timber.log.Timber

object WeatherIcons {

    fun getWeatherIconId(
        wmoCode: Int,
        isDay: Int,
        cloudCover: Int,
        iconStyle: IconStyles,
    ): Int? {
        val weatherCode = mapWeatherCode(wmoCode, isDay == 1 , cloudCover) ?: return null

        return when (iconStyle) {
            IconStyles.FLAT_OUTLINED_DARK -> IconsFlatOutlinedDark.getIconId(weatherCode)
            IconStyles.FLAT_OUTLINED_LIGHT -> null
            IconStyles.SOLID_3D_DARK -> null
            IconStyles.SOLID_3D_LIGHT -> null
        }
    }

    internal fun mapWeatherCode(wmoCode: Int, isDay: Boolean, cloudCover: Int): WeatherCodes? {
        val hasSky = cloudCover < MIN_CLOUD_COVER_CLOUDY

        return when (wmoCode) {

            0, 1, 2, 3 -> when {
                cloudCover > MIN_CLOUD_COVER_CLOUDY -> WeatherCodes.CLOUDY

                cloudCover > MIN_CLOUD_COVER_PARTLY_CLOUDY ->
                    if (isDay) WeatherCodes.PARTLY_CLOUDY_DAY else WeatherCodes.PARTLY_CLOUDY_NIGHT

                cloudCover > MIN_CLOUD_COVER_MAINLY_CLEAR ->
                    if (isDay) WeatherCodes.MAINLY_CLEAR_DAY else WeatherCodes.MAINLY_CLEAR_NIGHT

                else ->
                    if (isDay) WeatherCodes.CLEAR_SKY_DAY else WeatherCodes.CLEAR_SKY_NIGHT
            }

            45, 48 -> WeatherCodes.FOG

            51, 53, 55 -> when {
                hasSky && isDay -> WeatherCodes.DRIZZLE_AND_SKY_DAY
                hasSky && !isDay -> WeatherCodes.DRIZZLE_AND_SKY_NIGHT
                else -> WeatherCodes.DRIZZLE
            }

            56, 57 -> WeatherCodes.FREEZING_DRIZZLE

            61 -> when {
                hasSky && isDay -> WeatherCodes.LIGHT_RAIN_AND_SKY_DAY
                hasSky && !isDay -> WeatherCodes.LIGHT_RAIN_AND_SKY_NIGHT
                else -> WeatherCodes.LIGHT_RAIN
            }

            63 -> when {
                hasSky && isDay -> WeatherCodes.RAIN_AND_SKY_DAY
                hasSky && !isDay -> WeatherCodes.RAIN_AND_SKY_NIGHT
                else -> WeatherCodes.RAIN
            }

            65 -> when {
                hasSky && isDay -> WeatherCodes.HEAVY_RAIN_AND_SKY_DAY
                hasSky && !isDay -> WeatherCodes.HEAVY_RAIN_AND_SKY_NIGHT
                else -> WeatherCodes.HEAVY_RAIN
            }

            66 -> WeatherCodes.LIGHT_FREEZING_RAIN
            67 -> WeatherCodes.FREEZING_RAIN

            71 -> when {
                hasSky && isDay -> WeatherCodes.LIGHT_SNOW_AND_SKY_DAY
                hasSky && !isDay -> WeatherCodes.LIGHT_SNOW_AND_SKY_NIGHT
                else -> WeatherCodes.LIGHT_SNOW
            }

            73 -> when {
                hasSky && isDay -> WeatherCodes.SNOW_AND_SKY_DAY
                hasSky && !isDay -> WeatherCodes.SNOW_AND_SKY_NIGHT
                else -> WeatherCodes.SNOW
            }

            75 -> when {
                hasSky && isDay -> WeatherCodes.HEAVY_SNOW_AND_SKY_DAY
                hasSky && !isDay -> WeatherCodes.HEAVY_SNOW_AND_SKY_NIGHT
                else -> WeatherCodes.HEAVY_SNOW
            }

            77 -> WeatherCodes.SNOW_GRAINS

            80 -> when {
                hasSky && isDay -> WeatherCodes.LIGHT_RAIN_SHOWERS_AND_SKY_DAY
                hasSky && !isDay -> WeatherCodes.LIGHT_RAIN_SHOWERS_AND_SKY_NIGHT
                else -> WeatherCodes.LIGHT_RAIN_SHOWERS
            }

            81 -> when {
                hasSky && isDay -> WeatherCodes.RAIN_SHOWERS_AND_SKY_DAY
                hasSky && !isDay -> WeatherCodes.RAIN_SHOWERS_AND_SKY_NIGHT
                else -> WeatherCodes.RAIN_SHOWERS
            }

            82 -> when {
                hasSky && isDay -> WeatherCodes.HEAVY_RAIN_SHOWERS_AND_SKY_DAY
                hasSky && !isDay -> WeatherCodes.HEAVY_RAIN_SHOWERS_AND_SKY_NIGHT
                else -> WeatherCodes.HEAVY_RAIN_SHOWERS
            }

            85 -> when {
                hasSky && isDay -> WeatherCodes.LIGHT_SNOW_SHOWERS_AND_SKY_DAY
                hasSky && !isDay -> WeatherCodes.LIGHT_SNOW_SHOWERS_AND_SKY_NIGHT
                else -> WeatherCodes.LIGHT_SNOW_SHOWERS
            }

            86 -> when {
                hasSky && isDay -> WeatherCodes.HEAVY_SNOW_SHOWERS_AND_SKY_DAY
                hasSky && !isDay -> WeatherCodes.HEAVY_SNOW_SHOWERS_AND_SKY_NIGHT
                else -> WeatherCodes.HEAVY_SNOW_SHOWERS
            }

            95, 96, 99 -> when {
                hasSky && isDay -> WeatherCodes.THUNDERSTORM_AND_SKY_DAY
                hasSky && !isDay -> WeatherCodes.THUNDERSTORM_AND_SKY_NIGHT
                else -> WeatherCodes.THUNDERSTORM
            }

            else -> {
                Timber.w("mapWeatherCodes: Invalid WMO weather code")
                null
            }
        }
    }
}
