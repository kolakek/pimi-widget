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

import timber.log.Timber

object WeatherCodeMapper {

    internal fun mapWmoCode(
        wmoCode: Int,
        cloudCover: Int,
        precipProb: Int,
        visibility: Double,
        cape: Double
    ): WeatherCode? {
        val hasSky = cloudCover < MIN_CLOUD_COVER_OVERCAST

        return when (wmoCode) {

            0, 1, 2, 3, 45, 48 ->
                when {
                    visibility < MAX_VISIBILITY_FOG -> WeatherCode.FOG
                    cloudCover > MIN_CLOUD_COVER_OVERCAST -> WeatherCode.OVERCAST
                    cloudCover > MIN_CLOUD_COVER_MOSTLY_CLOUDY -> WeatherCode.MOSTLY_CLOUDY
                    cloudCover > MIN_CLOUD_COVER_PARTLY_CLOUDY -> WeatherCode.PARTLY_CLOUDY
                    cloudCover > MIN_CLOUD_COVER_MAINLY_CLEAR -> WeatherCode.MAINLY_CLEAR

                    else -> WeatherCode.CLEAR_SKY
                }

            51, 53, 55 ->
                if (hasSky) WeatherCode.DRIZZLE_AND_SKY
                else WeatherCode.DRIZZLE

            56, 57 ->
                WeatherCode.FREEZING_DRIZZLE

            61 ->
                if (hasSky) WeatherCode.LIGHT_RAIN_AND_SKY
                else WeatherCode.LIGHT_RAIN

            63 ->
                if (hasSky) WeatherCode.RAIN_AND_SKY
                else WeatherCode.RAIN

            65 ->
                if (hasSky) WeatherCode.HEAVY_RAIN_AND_SKY
                else WeatherCode.HEAVY_RAIN

            66 ->
                WeatherCode.LIGHT_FREEZING_RAIN

            67 ->
                WeatherCode.FREEZING_RAIN

            71 ->
                if (hasSky) WeatherCode.LIGHT_SNOW_AND_SKY
                else WeatherCode.LIGHT_SNOW

            73 ->
                if (hasSky) WeatherCode.SNOW_AND_SKY
                else WeatherCode.SNOW

            75 ->
                if (hasSky) WeatherCode.HEAVY_SNOW_AND_SKY
                else WeatherCode.HEAVY_SNOW

            77 ->
                WeatherCode.SNOW_GRAINS

            80 ->
                if (hasSky) WeatherCode.LIGHT_RAIN_SHOWERS_AND_SKY
                else WeatherCode.LIGHT_RAIN_SHOWERS

            81 ->
                if (hasSky) WeatherCode.RAIN_SHOWERS_AND_SKY
                else WeatherCode.RAIN_SHOWERS

            82 ->
                if (hasSky) WeatherCode.HEAVY_RAIN_SHOWERS_AND_SKY
                else WeatherCode.HEAVY_RAIN_SHOWERS

            85 ->
                if (hasSky) WeatherCode.LIGHT_SNOW_SHOWERS_AND_SKY
                else WeatherCode.LIGHT_SNOW_SHOWERS

            86 ->
                if (hasSky) WeatherCode.HEAVY_SNOW_SHOWERS_AND_SKY
                else WeatherCode.HEAVY_SNOW_SHOWERS

            95, 96 ->
                if (
                    cape > MIN_CAPE_THUNDERSTORM &&
                    precipProb > MIN_PROB_PRECIPITATION &&
                    cloudCover > MIN_CLOUD_COVER_OVERCAST
                ) {
                    WeatherCode.THUNDERSTORM
                } else if (
                    cape > MIN_CAPE_THUNDERSTORM &&
                    precipProb > MIN_PROB_PRECIPITATION
                ) {
                    WeatherCode.THUNDERSTORM_AND_SKY
                } else if (
                    precipProb > MIN_PROB_PRECIPITATION
                ) {
                    WeatherCode.POTENTIAL_THUNDERSTORM_AND_RAIN
                } else if (
                    cloudCover < MIN_CLOUD_COVER_OVERCAST
                ) {
                    WeatherCode.POTENTIAL_THUNDERSTORM_AND_SKY
                } else {
                    WeatherCode.POTENTIAL_THUNDERSTORM
                }

            99 ->
                WeatherCode.HEAVY_THUNDERSTORM

            else -> {
                Timber.w("mapWmoCode: Invalid WMO code")
                null
            }
        }
    }
}