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

internal object IconsSolid3dDark {

    fun getIconId(weatherCode: WeatherCodes, isDay: Boolean): Int {
        return when (weatherCode) {
            WeatherCodes.CLEAR_SKY ->
                if (isDay) R.drawable.wb_0d
                else R.drawable.wb_0n

            WeatherCodes.MAINLY_CLEAR ->
                if (isDay) R.drawable.wb_1d
                else R.drawable.wb_1n

            WeatherCodes.PARTLY_CLOUDY ->
                if (isDay) R.drawable.wb_2d
                else R.drawable.wb_2n

            WeatherCodes.MOSTLY_CLOUDY ->
                if (isDay) R.drawable.wb_3d
                else R.drawable.wb_3n

            WeatherCodes.OVERCAST ->
                R.drawable.wb_35

            WeatherCodes.FOGGY ->
                R.drawable.wb_45

            WeatherCodes.DRIZZLE,
            WeatherCodes.LIGHT_RAIN,
            WeatherCodes.LIGHT_RAIN_SHOWERS ->
                R.drawable.wb_61

            WeatherCodes.DRIZZLE_AND_SKY,
            WeatherCodes.LIGHT_RAIN_AND_SKY,
            WeatherCodes.LIGHT_RAIN_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wb_80d
                else R.drawable.wb_80n

            WeatherCodes.RAIN,
            WeatherCodes.RAIN_SHOWERS ->
                R.drawable.wb_63

            WeatherCodes.RAIN_AND_SKY,
            WeatherCodes.RAIN_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wb_81d
                else R.drawable.wb_81n

            WeatherCodes.HEAVY_RAIN,
            WeatherCodes.HEAVY_RAIN_SHOWERS ->
                R.drawable.wb_65

            WeatherCodes.HEAVY_RAIN_AND_SKY,
            WeatherCodes.HEAVY_RAIN_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wb_82d
                else R.drawable.wb_82n

            WeatherCodes.LIGHT_SNOW,
            WeatherCodes.LIGHT_SNOW_SHOWERS ->
                R.drawable.wb_71

            WeatherCodes.LIGHT_SNOW_AND_SKY,
            WeatherCodes.LIGHT_SNOW_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wb_85d
                else R.drawable.wb_85n

            WeatherCodes.SNOW ->
                R.drawable.wb_73

            WeatherCodes.SNOW_AND_SKY ->
                if (isDay) R.drawable.wb_86d
                else R.drawable.wb_86n

            WeatherCodes.HEAVY_SNOW,
            WeatherCodes.HEAVY_SNOW_SHOWERS ->
                R.drawable.wb_75

            WeatherCodes.HEAVY_SNOW_AND_SKY,
            WeatherCodes.HEAVY_SNOW_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.wb_87d
                else R.drawable.wb_87n

            WeatherCodes.SNOW_GRAINS ->
                R.drawable.wb_77

            WeatherCodes.FREEZING_DRIZZLE,
            WeatherCodes.LIGHT_FREEZING_RAIN,
            WeatherCodes.FREEZING_RAIN ->
                R.drawable.wb_67

            WeatherCodes.POTENTIAL_THUNDERSTORM_AND_SKY ->
                if (isDay) R.drawable.wb_91d
                else R.drawable.wb_91n

            WeatherCodes.POTENTIAL_THUNDERSTORM ->
                R.drawable.wb_92

            WeatherCodes.POTENTIAL_THUNDERSTORM_AND_RAIN ->
                R.drawable.wb_93

            WeatherCodes.THUNDERSTORM_AND_SKY ->
                if (isDay) R.drawable.wb_95d
                else R.drawable.wb_95n

            WeatherCodes.THUNDERSTORM,
            WeatherCodes.HEAVY_THUNDERSTORM ->
                R.drawable.wb_99
        }
    }
}
