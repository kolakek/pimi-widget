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

object WarningCodeMapper {

    internal fun getWarningCode(
        uvIndex: Int,
        uvIndexClearSky: Int,
        cloudCover: Double,
        apparentTempCelsius: Double,
        rain: Double,
        showers: Double,
        precipProb: Double
    ): WarningCode {
        for (warningCode in WarningCode.entries) {
            if (matchesWarning(
                    warningCode,
                    uvIndex,
                    uvIndexClearSky,
                    cloudCover,
                    apparentTempCelsius,
                    rain,
                    showers,
                    precipProb
                )
            ) return warningCode
        }
        return WarningCode.NO_WARNING
    }

    private fun matchesWarning(
        warningCode: WarningCode,
        uvIndex: Int,
        uvIndexClearSky: Int,
        cloudCover: Double,
        apparentTempCelsius: Double,
        rain: Double,
        showers: Double,
        precipProb: Double
    ): Boolean {
        return when (warningCode) {

            WarningCode.SEVERE_RAIN ->
                (rain + showers) > SEVR_RAIN_MIN_MM && precipProb > SEVR_RAIN_MIN_PRECIP_PROB

            WarningCode.EXTREME_HEAT ->
                apparentTempCelsius > EXTR_HEAT_MIN_APPARENT_TEMP_C

            WarningCode.SEVERE_HEAT ->
                apparentTempCelsius > SEVR_HEAT_MIN_APPARENT_TEMP_C

            WarningCode.EXTREME_UV ->
                (uvIndex >= EXTR_UV_MIN_UV_INDEX) ||
                        (uvIndexClearSky >= EXTR_UV_MIN_UV_INDEX &&
                                cloudCover < EXTR_UV_MAX_CLOUD_COVER)

            WarningCode.SEVERE_UV ->
                (uvIndex >= SEVR_UV_MIN_UV_INDEX) ||
                        (uvIndexClearSky >= SEVR_UV_MIN_UV_INDEX &&
                                cloudCover < SEVR_UV_MAX_CLOUD_COVER)

            WarningCode.NO_WARNING -> false
        }
    }
}