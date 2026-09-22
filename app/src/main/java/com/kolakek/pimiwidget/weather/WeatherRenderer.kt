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

import android.content.Context
import android.graphics.Color
import android.text.format.DateFormat
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.resources.ConditionIcon
import com.kolakek.pimiwidget.resources.ConditionString
import com.kolakek.pimiwidget.resources.WarningIcon
import com.kolakek.pimiwidget.resources.WarningString
import com.kolakek.pimiwidget.resources.WeatherIcon
import com.kolakek.pimiwidget.resources.WeatherString
import com.kolakek.pimiwidget.settings.AuxDisplay
import com.kolakek.pimiwidget.settings.IconColor
import com.kolakek.pimiwidget.settings.IconStyle
import com.kolakek.pimiwidget.settings.PressureUnit
import com.kolakek.pimiwidget.settings.TempUnit
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.settings.WindUnit
import com.kolakek.pimiwidget.widget.FORECAST_TODAY_HOUR_OFF
import com.kolakek.pimiwidget.widget.FORECAST_TODAY_HOUR_ON
import com.kolakek.pimiwidget.widget.FORECAST_TOMORROW_HOUR_OFF
import com.kolakek.pimiwidget.widget.FORECAST_TOMORROW_HOUR_ON
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt

object WeatherRenderer {

    fun currentWeather(
        context: Context,
        weather: WeatherData,
        tempUnit: TempUnit,
        iconStyle: IconStyle,
        iconColor: IconColor,
        fullUnit: Boolean = true
    ): LabeledIcon? {
        val weatherCode = weather.currentWeatherCode() ?: return null
        val tempCelsius = weather.currentTempCelsius() ?: return null
        val isDay = weather.currentIsDay() ?: return null

        val temperatureStr = temperatureString(context, tempCelsius, tempUnit, fullUnit)
        val weatherIconId = WeatherIcon.getWeatherIconId(weatherCode, isDay, iconStyle, iconColor)

        return LabeledIcon(temperatureStr, weatherIconId)
    }

    fun currentConditionString(
        context: Context,
        weather: WeatherData
    ): String? {
        val isDay = weather.currentIsDay() ?: return null
        val weatherCode = weather.currentWeatherCode() ?: return null

        return context.getString(WeatherString.shortStringId(weatherCode, isDay))
    }

    fun currentFeelsLikeString(
        context: Context,
        weather: WeatherData,
        tempUnit: TempUnit,
    ): String? {
        val currentApparentTemp = weather.currentApparentTempCelsius() ?: return null
        return context.getString(R.string.app_text_feels_like) + " " +
                temperatureString(context, currentApparentTemp, tempUnit, false)
    }

    fun currentWind(
        context: Context,
        weather: WeatherData,
        windUnit: WindUnit
    ): WeatherItem? {
        val windKmh = weather.currentWindSpeedKmh() ?: return null
        val directionDeg = weather.currentWindDirectionDeg() ?: return null

        val unitStr = if (windUnit == WindUnit.KMH) {
            context.getString(R.string.kmh)
        } else {
            context.getString(R.string.mph)
        }
        val gustsStr = weather.currentWindGustsKmh()?.let {
            context.getString(R.string.app_text_gusts) + ": " + speedString(it, windUnit)
        } ?: ""

        return WeatherItem(
            valueStr = speedString(windKmh, windUnit),
            unitStr = unitStr,
            auxStr = gustsStr,
            iconId = ConditionIcon.getWindIconId(),
            level = directionDeg,
        )
    }

    fun currentHumidity(
        context: Context,
        weather: WeatherData,
        tempUnit: TempUnit
    ): WeatherItem? {
        val humidity = weather.currentHumidity() ?: return null

        val dewPointStr = weather.currentDewPointCelsius()?.let {
            context.getString(R.string.app_text_dew_point) + ": " +
                    temperatureString(context, it, tempUnit, false)
        } ?: ""

        return WeatherItem(
            valueStr = "${humidity.toInt()}",
            unitStr = "%",
            auxStr = dewPointStr,
            iconId = ConditionIcon.getHumidityIconId(humidity),
            level = humidity
        )
    }

    fun currentUvIndex(
        context: Context,
        weather: WeatherData
    ): WeatherItem? {
        val uvIndex = weather.currentUvIndex() ?: return null

        return WeatherItem(
            valueStr = "${uvIndex.toInt()}",
            unitStr = "",
            auxStr = context.getString(ConditionString.getUvIndexStringId(uvIndex)),
            iconId = ConditionIcon.getUvIndexIconId(uvIndex),
            level = uvIndex
        )
    }

    fun currentPressure(
        context: Context,
        weather: WeatherData,
        pressureUnit: PressureUnit
    ): WeatherItem? {
        val pressureHpa = weather.currentPressureHpa() ?: return null

        val unitStr = when (pressureUnit) {
            PressureUnit.HPA -> context.getString(R.string.hpa)
            PressureUnit.MB -> context.getString(R.string.mb)
            PressureUnit.INHG -> context.getString(R.string.inhg)
        }
        val pressureStr = if (pressureUnit == PressureUnit.INHG) {
            String.format(Locale.getDefault(), "%.1f", pressureHpa * 0.02953)
        } else {
            String.format(Locale.getDefault(), "%,.0f", pressureHpa)
        }
        return WeatherItem(
            valueStr = pressureStr,
            unitStr = unitStr,
            auxStr = "",
            iconId = ConditionIcon.getPressureIconId(pressureHpa),
            level = pressureHpa
        )
    }

    fun currentWarning(
        context: Context,
        weather: WeatherData,
        prefs: WidgetPreferences
    ): LabeledIcon? {
        val warningCode = weather.nextHourlyWarningCode() ?: return null

        if (warningCode == WarningCode.NO_WARNING)
            return null

        return LabeledIcon(
            context.getString(WarningString.getWarningStrId(warningCode)),
            WarningIcon.getWarningIconId(warningCode.level, prefs.textColor, prefs.widgetStyle)
        )
    }

    fun forecastString(
        context: Context,
        weather: WeatherData,
        prefs: WidgetPreferences
    ): String? {
        val nowTimeMillis = System.currentTimeMillis()

        val zone = ZoneId.systemDefault()
        val zoned = Instant.ofEpochMilli(nowTimeMillis).atZone(zone)
        val date = zoned.toLocalDate()
        val hour = zoned.hour

        val useToday = when (hour) {
            in FORECAST_TODAY_HOUR_ON..<FORECAST_TODAY_HOUR_OFF -> true
            in FORECAST_TOMORROW_HOUR_ON..<FORECAST_TOMORROW_HOUR_OFF -> false

            else -> return null
        }
        val targetDate = if (useToday) date else date.plusDays(1)
        val idx = weather.dailyTimeMillis.indexOfFirst {
            Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == targetDate
        }
        if (idx == -1) {
            return null
        }
        val weatherCode = weather.dailyWeatherCode.getOrNull(idx) ?: return null
        val tempCelsiusMin = weather.dailyTempMinCelsius.getOrNull(idx) ?: return null
        val tempCelsiusMax = weather.dailyTempMaxCelsius.getOrNull(idx) ?: return null

        val minTempStr = temperatureString(context, tempCelsiusMin, prefs.tempUnit, false)
        val maxTempStr = temperatureString(context, tempCelsiusMax, prefs.tempUnit, false)

        val weatherStr = context.getString(
            WeatherString.shortStringId(weatherCode, isDay = true)
        )
        val dayStr = context.getString(
            if (useToday) R.string.widget_today else R.string.widget_tomorrow
        )
        return "$dayStr $maxTempStr/$minTempStr · $weatherStr"
    }

    fun dailyHighLowTempString(
        context: Context,
        weather: WeatherData,
        tempUnit: TempUnit,
    ): String? {
        val minTemp = weather.todayMinTempCelsius() ?: return null
        val maxTemp = weather.todayMaxTempCelsius() ?: return null

        val minTempStr = temperatureString(context, minTemp, tempUnit, false)
        val maxTempStr = temperatureString(context, maxTemp, tempUnit, false)

        return context.getString(R.string.app_text_max_temp) + ": " + maxTempStr + " · " +
                context.getString(R.string.app_text_min_temp) + ": " + minTempStr
    }

    fun dailySunInfo(
        context: Context,
        weather: WeatherData
    ): SunItem? {

        val sunriseMillis = weather.todaySunriseMillis() ?: return null
        val sunsetMillis = weather.todaySunsetMillis() ?: return null
        val currentMillis = System.currentTimeMillis()

        return SunItem(
            sunriseValueStr = timeMillisToStr(context, sunriseMillis),
            sunsetValueStr = timeMillisToStr(context, sunsetMillis),
            iconId = ConditionIcon.getSunTrackIconId(currentMillis, sunriseMillis, sunsetMillis)
        )
    }

    fun hourlyWeather(
        context: Context,
        weather: WeatherData,
        tempUnit: TempUnit,
        iconStyle: IconStyle,
        iconColor: IconColor,
    ): List<HourlyItem> {
        return weather.hourlyTimeMillis.indices
            .drop(weather.nextHourlyIndex().coerceAtLeast(0))
            .mapNotNull { idx ->
                val temp = weather.hourlyTempCelsius.getOrNull(idx) ?: return@mapNotNull null
                val code = weather.hourlyWeatherCode.getOrNull(idx) ?: return@mapNotNull null
                val isDay = weather.hourlyIsDay.getOrNull(idx) ?: return@mapNotNull null
                val timeMillis = weather.hourlyTimeMillis[idx]

                val iconId = WeatherIcon.getWeatherIconId(code, isDay, iconStyle, iconColor)
                val tempStr = temperatureString(context, temp, tempUnit, false)
                val timeStr = formatTime(context, timeMillis)

                val prob = weather.hourlyPrecipProb.getOrNull(idx)?.toInt() ?: 0
                val probStr = if (prob >= MIN_PROBABILITY_PRECIP_DISPLAY) "${prob}%" else ""

                HourlyItem(timeStr, iconId, tempStr, probStr)
            }
    }

    fun dailyWeather(
        context: Context,
        weather: WeatherData,
        tempUnit: TempUnit,
        iconStyle: IconStyle,
        iconColor: IconColor,
    ): List<DailyItem> {
        return weather.dailyTimeMillis.indices
            .drop(weather.todayIndex().coerceAtLeast(0))
            .mapNotNull { idx ->
                val minTemp = weather.dailyTempMinCelsius.getOrNull(idx) ?: return@mapNotNull null
                val maxTemp = weather.dailyTempMaxCelsius.getOrNull(idx) ?: return@mapNotNull null
                val code = weather.dailyWeatherCode.getOrNull(idx) ?: return@mapNotNull null
                val timeMillis = weather.dailyTimeMillis[idx]

                val iconId = WeatherIcon.getWeatherIconId(code, true, iconStyle, iconColor)
                val dateStr = formatDayName(context, timeMillis)

                val minTempStr = temperatureString(context, minTemp, tempUnit, false)
                val maxTempStr = temperatureString(context, maxTemp, tempUnit, false)
                val tempStr = "$maxTempStr/$minTempStr"

                val prob = weather.dailyPrecipProbMax.getOrNull(idx)?.toInt() ?: 0
                val probStr = if (prob >= MIN_PROBABILITY_PRECIP_DISPLAY) "${prob}%" else ""

                DailyItem(dateStr, iconId, tempStr, probStr)
            }
    }

    fun detailsRain(
        context: Context,
        weather: WeatherData
    ): DetailsItem {
        val barData = weather.hourlyTimeMillis.indices
            .drop(weather.nextHourlyIndex().coerceAtLeast(0))
            .mapNotNull { idx ->
                val rainMm = weather.hourlyRainMm.getOrNull(idx) ?: return@mapNotNull null
                val showersMm = weather.hourlyShowersMm.getOrNull(idx) ?: return@mapNotNull null
                val precipProb = weather.hourlyPrecipProb.getOrNull(idx) ?: return@mapNotNull null
                val timeMillis = weather.hourlyTimeMillis[idx]

                val sumMm = rainMm + showersMm

                val probStr = "${precipProb.toInt()}%"
                val valStr = if (sumMm > 0.0) "%.1f".format(sumMm) else ""
                val timeStr = formatTime(context, timeMillis)
                val level = (sumMm / RAIN_BAR_MAX_MM).coerceAtMost(1.0)

                val saturation = 0.1f + level.toFloat() * (0.61f - 0.1f)
                val fillColor = Color.HSVToColor(floatArrayOf(216f, saturation, 1.0f))
                val strokeColor = "#4C8DF6".toColorInt()

                DataBarItem(
                    valStr = valStr,
                    probStr = probStr,
                    level = level,
                    fillColor = fillColor,
                    strokeColor = strokeColor,
                    timeStr = timeStr
                )
            }
        val rainMm = weather.todayRainMm()
        val showersMm = weather.todayShowersMm()
        val valStr = (rainMm?.plus(showersMm ?: 0.0) ?: showersMm)?.let { "%.1f".format(it) }

        return DetailsItem(valStr, "mm", barData)
    }

    fun detailsHumidity(
        context: Context,
        weather: WeatherData
    ): DetailsItem {
        val barData = weather.hourlyTimeMillis.indices
            .drop(weather.nextHourlyIndex().coerceAtLeast(0))
            .mapNotNull { idx ->
                val humidity = weather.hourlyHumidity.getOrNull(idx) ?: return@mapNotNull null
                val timeMillis = weather.hourlyTimeMillis[idx]

                val probStr = "${humidity.toInt()}%"
                val timeStr = formatTime(context, timeMillis)
                val level = (humidity / 100).coerceAtMost(1.0)

                val h = 28f + (1f - level.toFloat()) * (48f - 28f)
                val b = 0.92f + (1f - level.toFloat()) * (0.98f - 0.92f)

                val fillColor = Color.HSVToColor(floatArrayOf(h, 0.85f, b))
                val strokeColor = "#D57A2D".toColorInt()

                DataBarItem(
                    valStr = "",
                    probStr = probStr,
                    level = level,
                    fillColor = fillColor,
                    strokeColor = strokeColor,
                    timeStr = timeStr
                )
            }
        val valStr = weather.todayHumidityMean()?.let { "${it.toInt()}" }

        return DetailsItem(valStr, "%", barData)
    }

    fun detailsUvIndex(
        context: Context,
        weather: WeatherData
    ): DetailsItem {
        val barData = weather.hourlyTimeMillis.indices
            .drop(weather.nextHourlyIndex().coerceAtLeast(0))
            .mapNotNull { idx ->
                val uvIndex = weather.hourlyUvIndex.getOrNull(idx)?.toInt() ?: return@mapNotNull null
                val timeMillis = weather.hourlyTimeMillis[idx]

                val valStr = "$uvIndex"
                val timeStr = formatTime(context, timeMillis)
                val level = (uvIndex.toDouble() / 11).coerceAtMost(1.0)

                val fillColor: Int
                val strokeColor: Int
                when (uvIndex) {
                    in 0 .. 2 -> {
                        fillColor = "#53c0a8".toColorInt()
                        strokeColor = "#53c0a8".toColorInt()
                    }
                    in 3 .. 5 -> {
                        fillColor = "#ffcc5c".toColorInt()
                        strokeColor = "#ffaa00".toColorInt()
                    }
                    in 6 .. 7 -> {
                        fillColor = "#f58259".toColorInt()
                        strokeColor = "#f45d2f".toColorInt()
                    }
                    in 8 .. 10 -> {
                        fillColor = "#f0516b".toColorInt()
                        strokeColor = "#f0516b".toColorInt()
                    }
                    else -> {
                        fillColor = "#7c67ae".toColorInt()
                        strokeColor = "#7c67ae".toColorInt()
                    }
                }
                DataBarItem(
                    valStr = valStr,
                    probStr = "",
                    level = level,
                    fillColor = fillColor,
                    strokeColor = strokeColor,
                    timeStr = timeStr
                )
            }
        val uvIndex = weather.todayUvIndexMax()
        val valStr = uvIndex?.let { "${it.toInt()}" }
        val unitStr = uvIndex?.let { context.getString(ConditionString.getUvIndexStringId(it)) }

        return DetailsItem(valStr, unitStr, barData)
    }

    fun auxString(
        context: Context,
        weather: WeatherData,
        prefs: WidgetPreferences
    ): String? {
        return when (prefs.auxDisplay) {

            AuxDisplay.NOTHING -> null

            AuxDisplay.UPDATE_TIME -> {
                val nowTimeMillis = System.currentTimeMillis()
                val str = timeMillisToStr(context, nowTimeMillis)
                context.getString(R.string.widget_updated_at) + " $str"
            }

            AuxDisplay.PLACE_CONDITION -> {
                if (weather.place.first().isDigit()) return null
                weather.place + " · " + currentConditionString(context, weather)
            }
        }
    }

    private fun temperatureString(
        context: Context,
        tempCelsius: Double,
        tempUnit: TempUnit,
        fullUnit: Boolean
    ): String {
        val isFahrenheit = (tempUnit == TempUnit.FAHRENHEIT)
        val tempValue = if (isFahrenheit) tempCelsius * 1.8 + 32 else tempCelsius
        val unit = if (fullUnit) {
            if (isFahrenheit) {
                context.getString(R.string.fahrenheit)
            } else {
                context.getString(R.string.celsius)
            }
        } else {
            context.getString(R.string.degree)
        }
        return "${(tempValue + 0.5).toInt()}$unit"
    }

    private fun speedString(speedKmh: Double, windUnit: WindUnit): String {
        val isMph = (windUnit == WindUnit.MPH)
        val speed = if (isMph) speedKmh * 0.621371 else speedKmh
        return "${(speed + 0.5).toInt()}"
    }

    private fun formatTime(context: Context, timeMillis: Long): String {
        val pattern = if (DateFormat.is24HourFormat(context)) "HH:00" else "h a"
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timeMillis))
    }

    private fun formatDayName(context: Context, timeMillis: Long): String {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(timeMillis).atZone(zone).toLocalDate()
        val today = LocalDate.now(zone)
        return if (date == today) {
            context.getString(R.string.widget_today)
        } else {
            SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timeMillis))
        }
    }

    private fun timeMillisToStr(context: Context, timeMillis: Long): String {
        return DateFormat.getTimeFormat(context).format(Date(timeMillis))
    }
}
