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

internal object IconsTwinkleShadowLight {

    fun getIconId(weatherCode: WeatherCode, isDay: Boolean): Int {
        return when (weatherCode) {
            WeatherCode.CLEAR_SKY ->
                if (isDay) R.drawable.wc_0d
                else R.drawable.wc_0n

            WeatherCode.MAINLY_CLEAR ->
                if (isDay) R.drawable.wc_1d
                else R.drawable.wc_1n

            WeatherCode.PARTLY_CLOUDY ->
                if (isDay) R.drawable.wc_2d
                else R.drawable.wc_2n

            WeatherCode.MOSTLY_CLOUDY ->
                if (isDay) R.drawable.wc_3d
                else R.drawable.wc_3n

            WeatherCode.OVERCAST ->
                R.drawable.wc_35

            WeatherCode.FOG ->
                R.drawable.wc_45

            WeatherCode.DRIZZLE,
            WeatherCode.LIGHT_RAIN,
            WeatherCode.LIGHT_RAIN_SHOWERS ->
                R.drawable.wc_61

            WeatherCode.DRIZZLE_AND_SKY,
            WeatherCode.LIGHT_RAIN_AND_SKY,
            WeatherCode.LIGHT_RAIN_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wc_80d
                else R.drawable.wc_80n

            WeatherCode.RAIN,
            WeatherCode.RAIN_SHOWERS ->
                R.drawable.wc_63

            WeatherCode.RAIN_AND_SKY,
            WeatherCode.RAIN_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wc_81d
                else R.drawable.wc_81n

            WeatherCode.HEAVY_RAIN,
            WeatherCode.HEAVY_RAIN_SHOWERS ->
                R.drawable.wc_65

            WeatherCode.HEAVY_RAIN_AND_SKY,
            WeatherCode.HEAVY_RAIN_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wc_82d
                else R.drawable.wc_82n

            WeatherCode.LIGHT_SNOW,
            WeatherCode.LIGHT_SNOW_SHOWERS ->
                R.drawable.wc_71

            WeatherCode.LIGHT_SNOW_AND_SKY,
            WeatherCode.LIGHT_SNOW_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wc_85d
                else R.drawable.wc_85n

            WeatherCode.SNOW ->
                R.drawable.wc_73

            WeatherCode.SNOW_AND_SKY ->
                if (isDay) R.drawable.wc_86d
                else R.drawable.wc_86n

            WeatherCode.HEAVY_SNOW,
            WeatherCode.HEAVY_SNOW_SHOWERS ->
                R.drawable.wc_75

            WeatherCode.HEAVY_SNOW_AND_SKY,
            WeatherCode.HEAVY_SNOW_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wc_87d
                else R.drawable.wc_87n

            WeatherCode.SNOW_GRAINS ->
                R.drawable.wc_77

            WeatherCode.FREEZING_DRIZZLE,
            WeatherCode.LIGHT_FREEZING_RAIN,
            WeatherCode.FREEZING_RAIN ->
                R.drawable.wc_67

            WeatherCode.THUNDERSTORM_AND_SKY ->
                if (isDay) R.drawable.wc_95d
                else R.drawable.wc_95n

            WeatherCode.THUNDERSTORM,
            WeatherCode.HEAVY_THUNDERSTORM ->
                R.drawable.wc_99
        }
    }
}