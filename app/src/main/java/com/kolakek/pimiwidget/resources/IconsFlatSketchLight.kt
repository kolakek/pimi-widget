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

internal object IconsFlatSketchLight {

    fun getIconId(weatherCode: WeatherCodes, isDay: Boolean): Int {
        return when (weatherCode) {
            WeatherCodes.CLEAR_SKY ->
                if (isDay) R.drawable.uc_0d
                else R.drawable.uc_0n

            WeatherCodes.MAINLY_CLEAR ->
                if (isDay) R.drawable.uc_1d
                else R.drawable.uc_1n

            WeatherCodes.PARTLY_CLOUDY ->
                if (isDay) R.drawable.uc_2d
                else R.drawable.uc_2n

            WeatherCodes.MOSTLY_CLOUDY ->
                if (isDay) R.drawable.uc_3d
                else R.drawable.uc_3n

            WeatherCodes.OVERCAST ->
                R.drawable.uc_35

            WeatherCodes.FOGGY ->
                R.drawable.uc_45

            WeatherCodes.DRIZZLE,
            WeatherCodes.LIGHT_RAIN,
            WeatherCodes.LIGHT_RAIN_SHOWERS ->
                R.drawable.uc_61

            WeatherCodes.DRIZZLE_AND_SKY,
            WeatherCodes.LIGHT_RAIN_AND_SKY,
            WeatherCodes.LIGHT_RAIN_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.uc_80d
                else R.drawable.uc_80n

            WeatherCodes.RAIN,
            WeatherCodes.RAIN_SHOWERS ->
                R.drawable.uc_63

            WeatherCodes.RAIN_AND_SKY,
            WeatherCodes.RAIN_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.uc_81d
                else R.drawable.uc_81n

            WeatherCodes.HEAVY_RAIN,
            WeatherCodes.HEAVY_RAIN_SHOWERS ->
                R.drawable.uc_65

            WeatherCodes.HEAVY_RAIN_AND_SKY,
            WeatherCodes.HEAVY_RAIN_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.uc_82d
                else R.drawable.uc_82n

            WeatherCodes.LIGHT_SNOW,
            WeatherCodes.LIGHT_SNOW_SHOWERS ->
                R.drawable.uc_71

            WeatherCodes.LIGHT_SNOW_AND_SKY,
            WeatherCodes.LIGHT_SNOW_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.uc_85d
                else R.drawable.uc_85n

            WeatherCodes.SNOW ->
                R.drawable.uc_73

            WeatherCodes.SNOW_AND_SKY ->
                if (isDay) R.drawable.uc_86d
                else R.drawable.uc_86n

            WeatherCodes.HEAVY_SNOW,
            WeatherCodes.HEAVY_SNOW_SHOWERS ->
                R.drawable.uc_75

            WeatherCodes.HEAVY_SNOW_AND_SKY,
            WeatherCodes.HEAVY_SNOW_SHOWERS_AND_SKY ->
                if (isDay) R.drawable.uc_87d
                else R.drawable.uc_87n

            WeatherCodes.SNOW_GRAINS ->
                R.drawable.uc_77

            WeatherCodes.FREEZING_DRIZZLE,
            WeatherCodes.LIGHT_FREEZING_RAIN,
            WeatherCodes.FREEZING_RAIN ->
                R.drawable.uc_67

            WeatherCodes.POTENTIAL_THUNDERSTORM_AND_SKY ->
                if (isDay) R.drawable.uc_91d
                else R.drawable.uc_91n

            WeatherCodes.POTENTIAL_THUNDERSTORM ->
                R.drawable.uc_92

            WeatherCodes.POTENTIAL_THUNDERSTORM_AND_RAIN ->
                R.drawable.uc_93

            WeatherCodes.THUNDERSTORM_AND_SKY ->
                if (isDay) R.drawable.uc_95d
                else R.drawable.uc_95n

            WeatherCodes.THUNDERSTORM,
            WeatherCodes.HEAVY_THUNDERSTORM ->
                R.drawable.uc_99
        }
    }
}
