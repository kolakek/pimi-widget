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

import com.kolakek.pimiwidget.settings.IconColor
import com.kolakek.pimiwidget.settings.IconStyle
import com.kolakek.pimiwidget.weather.WeatherCode

object WeatherIcon {

    fun getWeatherIconId(
        weatherCode: WeatherCode,
        isDay: Boolean,
        iconStyle: IconStyle,
        iconColor: IconColor
    ): Int {
        return when (iconStyle) {
            IconStyle.FLAT_SKETCH -> when (iconColor) {
                IconColor.DARK -> IconsFlatSketchDark.getIconId(weatherCode, isDay)
                IconColor.LIGHT -> IconsFlatSketchLight.getIconId(weatherCode, isDay)
                IconColor.THEMED -> IconsFlatSketchThemed.getIconId(weatherCode, isDay)
            }
            IconStyle.TWINKLE_SHADOW -> when (iconColor) {
                IconColor.DARK, IconColor.THEMED -> IconsTwinkleShadowDark.getIconId(weatherCode, isDay) // todo
                IconColor.LIGHT -> IconsTwinkleShadowLight.getIconId(weatherCode, isDay)
            }
        }
    }
}
