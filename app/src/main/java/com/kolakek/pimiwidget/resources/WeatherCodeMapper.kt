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

object WeatherCodeMapper {

    internal fun mapWmoCode(
        wmoCode: Int,
        cloudCover: Int,
        cape: Double
    ): WeatherCodes? {
        val hasSky = cloudCover < MIN_CLOUD_COVER_CLOUDY

        return when (wmoCode) {

            0, 1, 2, 3 ->
                when {
                    cloudCover > MIN_CLOUD_COVER_CLOUDY -> WeatherCodes.CLOUDY
                    cloudCover > MIN_CLOUD_COVER_PARTLY_CLOUDY -> WeatherCodes.PARTLY_CLOUDY
                    cloudCover > MIN_CLOUD_COVER_MAINLY_CLEAR -> WeatherCodes.MAINLY_CLEAR

                    else -> WeatherCodes.CLEAR_SKY
                }

            45, 48 ->
                WeatherCodes.FOGGY

            51, 53, 55 ->
                if (hasSky) WeatherCodes.DRIZZLE_AND_SKY
                else WeatherCodes.DRIZZLE

            56, 57 ->
                WeatherCodes.FREEZING_DRIZZLE

            61 ->
                if (hasSky) WeatherCodes.LIGHT_RAIN_AND_SKY
                else WeatherCodes.LIGHT_RAIN

            63 ->
                if (hasSky) WeatherCodes.RAIN_AND_SKY
                else WeatherCodes.RAIN

            65 ->
                if (hasSky) WeatherCodes.HEAVY_RAIN_AND_SKY
                else WeatherCodes.HEAVY_RAIN

            66 ->
                WeatherCodes.LIGHT_FREEZING_RAIN

            67 ->
                WeatherCodes.FREEZING_RAIN

            71 ->
                if (hasSky) WeatherCodes.LIGHT_SNOW_AND_SKY
                else WeatherCodes.LIGHT_SNOW

            73 ->
                if (hasSky) WeatherCodes.SNOW_AND_SKY
                else WeatherCodes.SNOW

            75 ->
                if (hasSky) WeatherCodes.HEAVY_SNOW_AND_SKY
                else WeatherCodes.HEAVY_SNOW

            77 ->
                WeatherCodes.SNOW_GRAINS

            80 ->
                if (hasSky) WeatherCodes.LIGHT_RAIN_SHOWERS_AND_SKY
                else WeatherCodes.LIGHT_RAIN_SHOWERS

            81 ->
                if (hasSky) WeatherCodes.RAIN_SHOWERS_AND_SKY
                else WeatherCodes.RAIN_SHOWERS

            82 ->
                if (hasSky) WeatherCodes.HEAVY_RAIN_SHOWERS_AND_SKY
                else WeatherCodes.HEAVY_RAIN_SHOWERS

            85 ->
                if (hasSky) WeatherCodes.LIGHT_SNOW_SHOWERS_AND_SKY
                else WeatherCodes.LIGHT_SNOW_SHOWERS

            86 ->
                if (hasSky) WeatherCodes.HEAVY_SNOW_SHOWERS_AND_SKY
                else WeatherCodes.HEAVY_SNOW_SHOWERS

            95, 96, 99 ->
                when {
                    cape > MIN_CAPE_HEAVY_THUNDERSTORM ->
                        if (hasSky) WeatherCodes.HEAVY_THUNDERSTORM_AND_SKY
                        else WeatherCodes.HEAVY_THUNDERSTORM

                    cape > MIN_CAPE_THUNDERSTORM ->
                        if (hasSky) WeatherCodes.THUNDERSTORM_AND_SKY
                        else WeatherCodes.THUNDERSTORM

                    else ->
                        if (hasSky) WeatherCodes.POTENTIAL_THUNDERSTORM_AND_SKY
                        else WeatherCodes.POTENTIAL_THUNDERSTORM
                }

            else -> {
                null
            }
        }
    }
}
