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
import com.kolakek.pimiwidget.weather.WeatherCode

internal object IconsTwinkleShadowDark {

    fun getIconId(weatherCode: WeatherCode, isDay: Boolean): Int {
        return when (weatherCode) {
            WeatherCode.CLEAR_SKY ->
                if (isDay) R.drawable.wb_0d
                else R.drawable.wb_0n

            WeatherCode.MAINLY_CLEAR ->
                if (isDay) R.drawable.wb_1d
                else R.drawable.wb_1n

            WeatherCode.PARTLY_CLOUDY ->
                if (isDay) R.drawable.wb_2d
                else R.drawable.wb_2n

            WeatherCode.MOSTLY_CLOUDY ->
                if (isDay) R.drawable.wb_3d
                else R.drawable.wb_3n

            WeatherCode.OVERCAST ->
                R.drawable.wb_35

            WeatherCode.FOG ->
                R.drawable.wb_45

            WeatherCode.DRIZZLE,
            WeatherCode.LIGHT_RAIN,
            WeatherCode.LIGHT_RAIN_SHOWERS ->
                R.drawable.wb_61

            WeatherCode.DRIZZLE_AND_SKY,
            WeatherCode.LIGHT_RAIN_AND_SKY,
            WeatherCode.LIGHT_RAIN_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wb_80d
                else R.drawable.wb_80n

            WeatherCode.RAIN,
            WeatherCode.RAIN_SHOWERS ->
                R.drawable.wb_63

            WeatherCode.RAIN_AND_SKY,
            WeatherCode.RAIN_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wb_81d
                else R.drawable.wb_81n

            WeatherCode.HEAVY_RAIN,
            WeatherCode.HEAVY_RAIN_SHOWERS ->
                R.drawable.wb_65

            WeatherCode.HEAVY_RAIN_AND_SKY,
            WeatherCode.HEAVY_RAIN_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wb_82d
                else R.drawable.wb_82n

            WeatherCode.LIGHT_SNOW,
            WeatherCode.LIGHT_SNOW_SHOWERS ->
                R.drawable.wb_71

            WeatherCode.LIGHT_SNOW_AND_SKY,
            WeatherCode.LIGHT_SNOW_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wb_85d
                else R.drawable.wb_85n

            WeatherCode.SNOW ->
                R.drawable.wb_73

            WeatherCode.SNOW_AND_SKY ->
                if (isDay) R.drawable.wb_86d
                else R.drawable.wb_86n

            WeatherCode.HEAVY_SNOW,
            WeatherCode.HEAVY_SNOW_SHOWERS ->
                R.drawable.wb_75

            WeatherCode.HEAVY_SNOW_AND_SKY,
            WeatherCode.HEAVY_SNOW_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wb_87d
                else R.drawable.wb_87n

            WeatherCode.SNOW_GRAINS ->
                R.drawable.wb_77

            WeatherCode.FREEZING_DRIZZLE,
            WeatherCode.LIGHT_FREEZING_RAIN,
            WeatherCode.FREEZING_RAIN ->
                R.drawable.wb_67

            WeatherCode.POTENTIAL_THUNDERSTORM_AND_SKY ->
                if (isDay) R.drawable.wb_91d
                else R.drawable.wb_91n

            WeatherCode.POTENTIAL_THUNDERSTORM ->
                R.drawable.wb_92

            WeatherCode.POTENTIAL_THUNDERSTORM_AND_RAIN ->
                R.drawable.wb_93

            WeatherCode.THUNDERSTORM_AND_SKY ->
                if (isDay) R.drawable.wb_95d
                else R.drawable.wb_95n

            WeatherCode.THUNDERSTORM,
            WeatherCode.HEAVY_THUNDERSTORM ->
                R.drawable.wb_99
        }
    }
}