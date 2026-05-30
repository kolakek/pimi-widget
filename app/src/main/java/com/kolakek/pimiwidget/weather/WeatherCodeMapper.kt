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

    internal fun getWeatherCode(
        wmoCode: Int,
        cloudCover: Double,
        precipProb: Double,
        visibility: Double,
        cape: Double
    ): WeatherCode? {
        val noSky = cloudCover > MIN_CLOUD_COVER_OVERCAST
        val noPrecip = precipProb < MIN_PROBABILITY_PRECIP
        val noFog = visibility > MAX_VISIBILITY_FOG
        val noThunderstorm = (cape < MIN_CAPE_THUNDERSTORM) || (precipProb < MIN_POP_THUNDERSTORM)

        val adjustedWmoCode = when (wmoCode) {
            in 45..48 -> if (noFog) CODE_NO_PRECIP else wmoCode

            in 51..86 -> if (noPrecip) CODE_NO_PRECIP else wmoCode

            in 95..99 -> if (noThunderstorm) {
                if (noPrecip) CODE_NO_PRECIP else CODE_LIGHT_SHOWERS
            } else {
                wmoCode
            }

            else -> wmoCode
        }
        return when (adjustedWmoCode) {

            0, 1, 2, 3, CODE_NO_PRECIP ->
                when {
                    cloudCover > MIN_CLOUD_COVER_OVERCAST -> WeatherCode.OVERCAST
                    cloudCover > MIN_CLOUD_COVER_MOSTLY_CLOUDY -> WeatherCode.MOSTLY_CLOUDY
                    cloudCover > MIN_CLOUD_COVER_PARTLY_CLOUDY -> WeatherCode.PARTLY_CLOUDY
                    cloudCover > MIN_CLOUD_COVER_MAINLY_CLEAR -> WeatherCode.MAINLY_CLEAR

                    else -> WeatherCode.CLEAR_SKY
                }

            45, 48 ->
                WeatherCode.FOG

            51, 53, 55 ->
                if (noSky) WeatherCode.DRIZZLE
                else WeatherCode.DRIZZLE_AND_SKY

            56, 57 ->
                WeatherCode.FREEZING_DRIZZLE

            61 ->
                if (noSky) WeatherCode.LIGHT_RAIN
                else WeatherCode.LIGHT_RAIN_AND_SKY

            63 ->
                if (noSky) WeatherCode.RAIN
                else WeatherCode.RAIN_AND_SKY

            65 ->
                if (noSky) WeatherCode.HEAVY_RAIN
                else WeatherCode.HEAVY_RAIN_AND_SKY

            66 ->
                WeatherCode.LIGHT_FREEZING_RAIN

            67 ->
                WeatherCode.FREEZING_RAIN

            71 ->
                if (noSky) WeatherCode.LIGHT_SNOW
                else WeatherCode.LIGHT_SNOW_AND_SKY

            73 ->
                if (noSky) WeatherCode.SNOW
                else WeatherCode.SNOW_AND_SKY

            75 ->
                if (noSky) WeatherCode.HEAVY_SNOW
                else WeatherCode.HEAVY_SNOW_AND_SKY

            77 ->
                WeatherCode.SNOW_GRAINS

            80, CODE_LIGHT_SHOWERS ->
                if (noSky) WeatherCode.LIGHT_RAIN_SHOWERS
                else WeatherCode.LIGHT_RAIN_SHOWERS_AND_SKY

            81 ->
                if (noSky) WeatherCode.RAIN_SHOWERS
                else WeatherCode.RAIN_SHOWERS_AND_SKY

            82 ->
                if (noSky) WeatherCode.HEAVY_RAIN_SHOWERS
                else WeatherCode.HEAVY_RAIN_SHOWERS_AND_SKY

            85 ->
                if (noSky) WeatherCode.LIGHT_SNOW_SHOWERS
                else WeatherCode.LIGHT_SNOW_SHOWERS_AND_SKY

            86 ->
                if (noSky) WeatherCode.HEAVY_SNOW_SHOWERS
                else WeatherCode.HEAVY_SNOW_SHOWERS_AND_SKY

            95, 96 ->
                if (noSky) WeatherCode.THUNDERSTORM
                else WeatherCode.THUNDERSTORM_AND_SKY

            99 ->
                WeatherCode.HEAVY_THUNDERSTORM

            else -> {
                Timber.w("mapWmoCode: Invalid WMO code")
                null
            }
        }
    }
}