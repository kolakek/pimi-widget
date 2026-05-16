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

object WeatherIcons {

    fun getWeatherIconId(
        wmoCode: Int,
        isDay: Int,
        cloudCover: Int,
        precipProb: Int,
        visibility: Double,
        cape: Double,
        iconStyle: IconStyles,
    ): Int? {
        val weatherCode = WeatherCodeMapper.mapWmoCode(
            wmoCode,
            cloudCover,
            precipProb,
            visibility,
            cape
        ) ?: return null

        return when (iconStyle) {
            IconStyles.FLAT_SKETCH_DARK ->
                IconsFlatSketchDark.getIconId(weatherCode, isDay == 1)

            IconStyles.FLAT_SKETCH_LIGHT ->
                IconsFlatSketchLight.getIconId(weatherCode, isDay == 1)

            IconStyles.TWINKLE_SHADOW_DARK ->
                IconsTwinkleShadowDark.getIconId(weatherCode, isDay == 1)

            IconStyles.TWINKLE_SHADOW_LIGHT ->
                IconsTwinkleShadowLight.getIconId(weatherCode, isDay == 1)
        }
    }
}
