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
        uvIndex: Double,
        uvIndexClearSky: Double,
        cloudCover: Double
    ): WarningCode {
        val extremeUv = (uvIndex >= EXTR_UV_MIN_UV_INDEX) ||
                (uvIndexClearSky >= EXTR_UV_MIN_UV_INDEX && cloudCover < EXTR_UV_MAX_CLOUD_COVER)

        val severeUv = (uvIndex >= SEVR_UV_MIN_UV_INDEX) ||
                (uvIndexClearSky >= SEVR_UV_MIN_UV_INDEX && cloudCover < SEVR_UV_MAX_CLOUD_COVER)

        return when {
            extremeUv -> WarningCode.EXTREME_UV

            severeUv -> WarningCode.SEVERE_UV

            else -> WarningCode.NO_WARNING
        }
    }
}