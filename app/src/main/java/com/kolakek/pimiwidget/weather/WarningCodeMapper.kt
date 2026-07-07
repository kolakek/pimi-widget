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

    fun getWarningCode(
        wmoCode: Int,
        uvIndex: Int,
        uvIndexClearSky: Int,
        cloudCover: Double,
        apparentTempCelsius: Double,
        rain: Double,
        rainProb: Double,
        cape: Double,
        windGusts: Double
    ): WarningCode {
        for (warningCode in WarningCode.entries) {
            if (matchesWarning(
                    warningCode = warningCode,
                    wmoCode = wmoCode,
                    uvIndex = uvIndex,
                    uvIndexClearSky = uvIndexClearSky,
                    cloudCover = cloudCover,
                    apparentTempCelsius = apparentTempCelsius,
                    rain = rain,
                    rainProb = rainProb,
                    cape = cape,
                    windGusts = windGusts
                )
            ) return warningCode
        }
        return WarningCode.NO_WARNING
    }

    private fun matchesWarning(
        warningCode: WarningCode,
        wmoCode: Int,
        uvIndex: Int,
        uvIndexClearSky: Int,
        cloudCover: Double,
        apparentTempCelsius: Double,
        rain: Double,
        rainProb: Double,
        cape: Double,
        windGusts: Double
    ): Boolean {
        return when (warningCode) {

            WarningCode.EXTREME_TSTORM ->
                (isExtremeGusts(windGusts) || isExtremeRain(rain, rainProb)) &&
                        (cape >= WARN_XTR_TSTORM_MIN_CAPE) && (wmoCode in 95..99)

            WarningCode.SEVERE_TSTORM ->
                (isSevereGusts(windGusts) || isSevereRain(rain, rainProb)) &&
                        (cape >= WARN_SVR_TSTORM_MIN_CAPE) && (wmoCode in 95..99)

            WarningCode.EXTREME_GUSTS ->
                isExtremeGusts(windGusts)

            WarningCode.SEVERE_GUSTS ->
                isSevereGusts(windGusts)

            WarningCode.EXTREME_RAIN ->
                isExtremeRain(rain, rainProb)

            WarningCode.SEVERE_RAIN ->
                isSevereRain(rain, rainProb)

            WarningCode.EXTREME_HEAT ->
                apparentTempCelsius >= WARN_XTR_HEAT_MIN_APPARENT_TEMP_C

            WarningCode.SEVERE_HEAT ->
                apparentTempCelsius >= WARN_SVR_HEAT_MIN_APPARENT_TEMP_C

            WarningCode.EXTREME_UV ->
                (uvIndex >= WARN_XTR_UV_MIN_UV_INDEX) ||
                        (uvIndexClearSky >= WARN_XTR_UV_MIN_UV_INDEX &&
                                cloudCover <= WARN_XTR_UV_MAX_CLOUD_COVER)

            WarningCode.SEVERE_UV ->
                (uvIndex >= WARN_SVR_UV_MIN_UV_INDEX) ||
                        (uvIndexClearSky >= WARN_SVR_UV_MIN_UV_INDEX &&
                                cloudCover <= WARN_SVR_UV_MAX_CLOUD_COVER)

            WarningCode.NO_WARNING -> false
        }
    }

    private fun isExtremeRain(rain: Double, rainProb: Double) =
        rain >= WARN_XTR_RAIN_MIN_MM && rainProb >= WARN_XTR_RAIN_MIN_PROB

    private fun isSevereRain(rain: Double, rainProb: Double) =
        rain >= WARN_SVR_RAIN_MIN_MM && rainProb >= WARN_SVR_RAIN_MIN_PROB

    private fun isExtremeGusts(windGusts: Double) =
        windGusts >= WARN_XTR_GUSTS_MIN_KMH

    private fun isSevereGusts(windGusts: Double) =
        windGusts >= WARN_SVR_GUSTS_MIN_KMH
}
