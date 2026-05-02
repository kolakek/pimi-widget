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

package com.kolakek.pimiwidget.widget

import android.content.Context
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.weather.WeatherData
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId

internal object WeatherFormatter {

    internal fun getCurrentWeatherStrAndIcon(
        context: Context,
        weather: WeatherData,
        nowTimeMillis: Long,
        tempUnit: String,
        iconStyle: String,
        lightText: Boolean
    ): TextWithIcon? {
        val idx = weather.minutelyTimeMillis.indexOfFirst { it > nowTimeMillis }

        if (idx == -1) {
            Timber.w("getCurrentWeatherStrAndIcon: No data available for the next 15min")
            return null
        }
        Timber.d("getCurrentWeatherStrAndIcon: Current weather at index $idx")

        val tempCelsius = weather.minutelyTempCelsius.getOrNull(idx) ?: return null
        val weatherCode = weather.minutelyWeatherCode.getOrNull(idx) ?: return null
        val isDay = weather.minutelyIsDay.getOrNull(idx) ?: return null

        val str = getTemperatureStr(context, tempCelsius, tempUnit)
        val id = mapWeatherId(weatherCode, isDay, iconStyle, lightText) ?: return null

        return TextWithIcon(str, id)
    }

    internal fun getForecastStr(
        context: Context,
        nowTimeMillis: Long,
        weather: WeatherData,
        tempUnit: String
    ): String? {

        val zone = ZoneId.systemDefault()
        val zoned = Instant.ofEpochMilli(nowTimeMillis).atZone(zone)
        val date = zoned.toLocalDate()
        val hour = zoned.hour

        val isToday = when (hour) {
            in FORECAST_TODAY_HOUR_ON..<FORECAST_TODAY_HOUR_OFF -> true
            in FORECAST_TOMORROW_HOUR_ON..<FORECAST_TOMORROW_HOUR_OFF -> false

            else -> return null
        }
        val targetDate = if (isToday) date else date.plusDays(1)
        val idx = weather.dailyTimeMillis.indexOfFirst {
            Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == targetDate
        }
        Timber.d("getForecastStr: Target date $targetDate at index $idx")

        if (idx == -1) {
            Timber.w("getForecastStr: No forecast data available")
            return null
        }
        val tempCelsiusMin = weather.dailyTempMinCelsius.getOrNull(idx) ?: return null
        val tempCelsiusMax = weather.dailyTempMaxCelsius.getOrNull(idx) ?: return null
        val weatherCode = weather.hourlyWeatherCode.getOrNull(idx) ?: return null
        val rainSum = weather.dailyRainSum.getOrNull(idx) ?: return null
        val showersSum = weather.dailyShowersSum.getOrNull(idx) ?: return null
        val snowSum = weather.dailySnowfallSum.getOrNull(idx) ?: return null
        val visibilityMean = weather.dailyVisibilityMean.getOrNull(idx) ?: return null
        val cloudCoverMean = weather.dailyCloudCoverMean.getOrNull(idx) ?: return null

        val minTempStr = getTemperatureStr(context, tempCelsiusMin, tempUnit, false)
        val maxTempStr = getTemperatureStr(context, tempCelsiusMax, tempUnit, false)

        val codeStrId = getWeatherCodeId(
            weatherCode,
            rainSum,
            showersSum,
            snowSum,
            visibilityMean,
            cloudCoverMean
        )
        val dayStrId = if (isToday) R.string.today else R.string.tomorrow

        return context.getString(
            R.string.forecast_line,
            context.getString(dayStrId),
            context.getString(codeStrId),
            maxTempStr,
            minTempStr
        )
    }

    private fun getTemperatureStr(
        context: Context,
        tempCelsius: Double,
        tempUnit: String,
        fullUnit: Boolean = true
    ): String {

        val isFahrenheit = (tempUnit == KEY_FAHRENHEIT)
        val temp = if (isFahrenheit) tempCelsius * 1.8 + 32 else tempCelsius
        val unit = when {
            fullUnit && isFahrenheit -> context.getString(R.string.fahrenheit)
            fullUnit -> context.getString(R.string.celsius)
            else -> context.getString(R.string.degree)
        }
        return "${(temp+0.5).toInt()}$unit"
    }

    private fun getWeatherCodeId(
        code: Int,
        rainSum: Double,
        showersSum: Double,
        snowSum: Double,
        visibility: Double,
        cloudCover: Int
    ): Int {
        val rainShowerSum = rainSum  + showersSum

        Timber.d("getWeatherCodeId: code = $code, rain = $rainShowerSum, snow = $snowSum, " +
                "cloud = $cloudCover, vis = $visibility")

        return when {

            code in setOf(95, 96, 99) -> R.string.thunderstorms
            code in setOf(57, 67) -> R.string.freezing_rain
            code in setOf(56, 66) -> R.string.freezing_drizzle
            snowSum > HEAVY_SNOW_CM -> R.string.heavy_snow
            rainShowerSum > HEAVY_RAIN_MM -> R.string.heavy_rain
            snowSum > SNOW_CM -> R.string.snow_showers
            rainShowerSum > RAIN_MM -> R.string.rain_showers
            snowSum > FLURRIES_CM -> R.string.flurries
            rainShowerSum > DRIZZLE_MM -> R.string.drizzle
            visibility < FOG_VISIBILITY_M -> R.string.foggy
            cloudCover > CLOUDY_PERCENT -> R.string.cloudy
            cloudCover > MOSTLY_CLOUDY_PERCENT -> R.string.mostly_cloudy
            cloudCover > PARTLY_CLOUDY_PERCENT -> R.string.partly_cloudy
            cloudCover > MOSTLY_CLEAR_PERCENT -> R.string.mostly_clear

            else -> R.string.clear
        }
    }

    private fun mapWeatherId(
        code: Int,
        isDay: Int,
        style: String,
        darkColor: Boolean
    ): Int? {
        return when {
            (style == KEY_ICON_STYLE_FILLED && darkColor) -> iconIdFilledDark(code, isDay)
            (style == KEY_ICON_STYLE_FILLED) -> iconIdFilled(code, isDay)
            (style == KEY_ICON_STYLE_OUTLINED && darkColor) -> iconIdOutlinedDark(code, isDay)
            (style == KEY_ICON_STYLE_OUTLINED) -> iconIdOutlined(code, isDay)

            else -> {
                Timber.w("mapWeatherId: Unexpected null return")
                null
            }
        }
    }

    private fun iconIdFilled(code: Int, isDay: Int): Int? {
        return when (code) {

            0 -> if (isDay == 1) R.drawable.wb_0d else R.drawable.wb_0n
            1 -> if (isDay == 1) R.drawable.wb_1d else R.drawable.wb_1n
            2 -> if (isDay == 1) R.drawable.wb_2d else R.drawable.wb_2n
            3 -> R.drawable.wb_3
            45, 48 -> R.drawable.wb_45
            51, 61 -> R.drawable.wb_61
            53, 63 -> R.drawable.wb_63
            55, 65 -> R.drawable.wb_65
            56, 57, 66, 67 -> R.drawable.wb_67
            71 -> R.drawable.wb_71
            73 -> R.drawable.wb_73
            75 -> R.drawable.wb_75
            77 -> R.drawable.wb_77
            80 -> if (isDay == 1) R.drawable.wb_80d else R.drawable.wb_80n
            81 -> if (isDay == 1) R.drawable.wb_81d else R.drawable.wb_81n
            82 -> if (isDay == 1) R.drawable.wb_82d else R.drawable.wb_82n
            85 -> if (isDay == 1) R.drawable.wb_85d else R.drawable.wb_85n
            86 -> if (isDay == 1) R.drawable.wb_86d else R.drawable.wb_86n
            95 -> if (isDay == 1) R.drawable.wb_95d else R.drawable.wb_95n
            96 -> if (isDay == 1) R.drawable.wb_96d else R.drawable.wb_96n
            99 -> R.drawable.wb_99

            else -> {
                Timber.w("iconIdFilled: Invalid weather code")
                null
            }
        }
    }

    private fun iconIdFilledDark(code: Int, isDay: Int): Int? {
        return when (code) {

            0 -> if (isDay == 1) R.drawable.wc_0d else R.drawable.wc_0n
            1 -> if (isDay == 1) R.drawable.wc_1d else R.drawable.wc_1n
            2 -> if (isDay == 1) R.drawable.wc_2d else R.drawable.wc_2n
            3 -> R.drawable.wc_3
            45, 48 -> R.drawable.wc_45
            51, 61 -> R.drawable.wc_61
            53, 63 -> R.drawable.wc_63
            55, 65 -> R.drawable.wc_65
            56, 57, 66, 67 -> R.drawable.wc_67
            71 -> R.drawable.wc_71
            73 -> R.drawable.wc_73
            75 -> R.drawable.wc_75
            77 -> R.drawable.wc_77
            80 -> if (isDay == 1) R.drawable.wc_80d else R.drawable.wc_80n
            81 -> if (isDay == 1) R.drawable.wc_81d else R.drawable.wc_81n
            82 -> if (isDay == 1) R.drawable.wc_82d else R.drawable.wc_82n
            85 -> if (isDay == 1) R.drawable.wc_85d else R.drawable.wc_85n
            86 -> if (isDay == 1) R.drawable.wc_86d else R.drawable.wc_86n
            95 -> if (isDay == 1) R.drawable.wc_95d else R.drawable.wc_95n
            96 -> if (isDay == 1) R.drawable.wc_96d else R.drawable.wc_96n
            99 -> R.drawable.wc_99

            else -> {
                Timber.w("iconIdFilledDark: Invalid weather code")
                null
            }
        }
    }

    private fun iconIdOutlined(code: Int, isDay: Int): Int? {
        return when (code) {

            0 -> if (isDay == 1) R.drawable.ub_0d else R.drawable.ub_0n
            1 -> if (isDay == 1) R.drawable.ub_1d else R.drawable.ub_1n
            2 -> if (isDay == 1) R.drawable.ub_2d else R.drawable.ub_2n
            3 -> R.drawable.ub_3
            45, 48 -> R.drawable.ub_45
            51, 61 -> R.drawable.ub_61
            53, 63 -> R.drawable.ub_63
            55, 65 -> R.drawable.ub_65
            56, 57, 66, 67 -> R.drawable.ub_67
            71 -> R.drawable.ub_71
            73 -> R.drawable.ub_73
            75 -> R.drawable.ub_75
            77 -> R.drawable.ub_77
            80 -> if (isDay == 1) R.drawable.ub_80d else R.drawable.ub_80n
            81 -> if (isDay == 1) R.drawable.ub_81d else R.drawable.ub_81n
            82 -> if (isDay == 1) R.drawable.ub_82d else R.drawable.ub_82n
            85 -> if (isDay == 1) R.drawable.ub_85d else R.drawable.ub_85n
            86 -> if (isDay == 1) R.drawable.ub_86d else R.drawable.ub_86n
            95 -> if (isDay == 1) R.drawable.ub_95d else R.drawable.ub_95n
            96 -> if (isDay == 1) R.drawable.ub_96d else R.drawable.ub_96n
            99 -> R.drawable.ub_99

            else -> {
                Timber.w("iconIdOutlined: Invalid weather code")
                null
            }
        }
    }

    private fun iconIdOutlinedDark(code: Int, isDay: Int): Int? {
        return when (code) {

            0 -> if (isDay == 1) R.drawable.uc_0d else R.drawable.uc_0n
            1 -> if (isDay == 1) R.drawable.uc_1d else R.drawable.uc_1n
            2 -> if (isDay == 1) R.drawable.uc_2d else R.drawable.uc_2n
            3 -> R.drawable.uc_3
            45, 48 -> R.drawable.uc_45
            51, 61 -> R.drawable.uc_61
            53, 63 -> R.drawable.uc_63
            55, 65 -> R.drawable.uc_65
            56, 57, 66, 67 -> R.drawable.uc_67
            71 -> R.drawable.uc_71
            73 -> R.drawable.uc_73
            75 -> R.drawable.uc_75
            77 -> R.drawable.uc_77
            80 -> if (isDay == 1) R.drawable.uc_80d else R.drawable.uc_80n
            81 -> if (isDay == 1) R.drawable.uc_81d else R.drawable.uc_81n
            82 -> if (isDay == 1) R.drawable.uc_82d else R.drawable.uc_82n
            85 -> if (isDay == 1) R.drawable.uc_85d else R.drawable.uc_85n
            86 -> if (isDay == 1) R.drawable.uc_86d else R.drawable.uc_86n
            95 -> if (isDay == 1) R.drawable.uc_95d else R.drawable.uc_95n
            96 -> if (isDay == 1) R.drawable.uc_96d else R.drawable.uc_96n
            99 -> R.drawable.uc_99

            else -> {
                Timber.w("iconIdOutlinedDark: Invalid weather code")
                null
            }
        }
    }
}
