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

package com.kolakek.pimiwidget.ui

import android.content.Context
import com.kolakek.pimiwidget.settings.AppPreferences
import com.kolakek.pimiwidget.settings.IconColor
import com.kolakek.pimiwidget.weather.DailyItem
import com.kolakek.pimiwidget.weather.HourlyItem
import com.kolakek.pimiwidget.weather.LabeledIcon
import com.kolakek.pimiwidget.weather.WeatherData
import com.kolakek.pimiwidget.weather.WeatherItem
import com.kolakek.pimiwidget.weather.WeatherRenderer

data class DisplayData (
    val currentWeather: LabeledIcon?,
    val currentCondition: String?,
    val currentFeelsLike: String?,
    val dailyHighLowTemp: String?,
    val hourlyWeather: List<HourlyItem>,
    val dailyWeather: List<DailyItem>,
    val currentWind: WeatherItem?,
    val currentHumidity: WeatherItem?,
    val currentPressure: WeatherItem?,
    val currentUvIndex: WeatherItem?,
    val place: String
) {
    fun isValid(): Boolean {
        return currentWeather != null && hourlyWeather.size >= 5 && dailyWeather.size >= 3
    }

    companion object {
        fun collect(
            context: Context,
            weather: WeatherData,
            prefs: AppPreferences
        ): DisplayData {
            val currentWeather = WeatherRenderer.currentWeather(
                context,
                weather,
                prefs.tempUnit,
                prefs.iconStyle,
                IconColor.THEMED,
                fullUnit = false
            )
            val currentConditionString = WeatherRenderer.currentConditionString(
                context,
                weather
            )
            val currentFeelsLikeString = WeatherRenderer.currentFeelsLikeString(
                context,
                weather,
                prefs.tempUnit
            )
            val dailyHighLowTempString = WeatherRenderer.dailyHighLowTempString(
                context,
                weather,
                prefs.tempUnit
            )
            val hourlyWeather = WeatherRenderer.hourlyWeather(
                context,
                weather,
                prefs.tempUnit,
                prefs.iconStyle,
                IconColor.THEMED
            )
            val dailyWeather = WeatherRenderer.dailyWeather(
                context,
                weather,
                prefs.tempUnit,
                prefs.iconStyle,
                IconColor.THEMED
            )
            val currentWind = WeatherRenderer.currentWind(
                context,
                weather
            )
            val currentHumidity = WeatherRenderer.currentHumidity(
                context,
                weather,
                prefs.tempUnit
            )
            val currentPressure = WeatherRenderer.currentPressure(
                context,
                weather
            )
            val currentUvIndex = WeatherRenderer.currentUvIndex(
                context,
                weather
            )
            return DisplayData(
                currentWeather = currentWeather,
                currentCondition = currentConditionString,
                currentFeelsLike = currentFeelsLikeString,
                dailyHighLowTemp = dailyHighLowTempString,
                hourlyWeather = hourlyWeather,
                dailyWeather = dailyWeather,
                currentWind = currentWind,
                currentHumidity = currentHumidity,
                currentPressure = currentPressure,
                currentUvIndex = currentUvIndex,
                place = weather.place)
        }
    }
}

