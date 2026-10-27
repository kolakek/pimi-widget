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
import com.kolakek.pimiwidget.settings.RainUnit
import com.kolakek.pimiwidget.settings.SnowUnit
import kotlin.math.round

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

        val windStr = String.format(
            Locale.getDefault(),
            windUnit.format,
            windUnit.fromKmh(windKmh)
        )
        val gustsStr = weather.currentWindGustsKmh()?.let {
            context.getString(R.string.app_text_gusts) + ": " + String.format(
                Locale.getDefault(),
                windUnit.format,
                windUnit.fromKmh(it)
            )
        }
        return WeatherItem(
            valueStr = windStr,
            unitStr = windUnit.unitStr(context),
            auxStr = gustsStr,
            image = ConditionIcon.windImage(directionDeg)
        )
    }

    fun currentHumidity(
        context: Context,
        weather: WeatherData,
        tempUnit: TempUnit
    ): WeatherItem? {
        val humidity = weather.currentHumidity() ?: return null

        val image = ConditionIcon.humidityImage(humidity)

        val dewPointStr = weather.currentDewPointCelsius()?.let {
            context.getString(R.string.app_text_dew_point) + ": " +
                    temperatureString(context, it, tempUnit, false)
        }
        return WeatherItem(
            valueStr = "${humidity.toInt()}",
            unitStr = "%",
            auxStr = dewPointStr,
            image = image
        )
    }

    fun currentUvIndex(
        context: Context,
        weather: WeatherData
    ): WeatherItem? {
        val uvIndex = weather.currentUvIndex()?.let { round(it) } ?: return null
        return WeatherItem(
            valueStr = "${uvIndex.toInt()}",
            auxStr = context.getString(ConditionString.getUvIndexStringId(uvIndex)),
            image = ConditionIcon.uvIndexImage(uvIndex)
        )
    }

    fun currentPressure(
        context: Context,
        weather: WeatherData,
        pressureUnit: PressureUnit
    ): WeatherItem? {
        val pressureHpa = weather.currentPressureHpa() ?: return null

        val pressureStr = String.format(
            Locale.getDefault(),
            pressureUnit.format,
            pressureUnit.fromHpa(pressureHpa)
        )
        return WeatherItem(
            valueStr = pressureStr,
            unitStr = pressureUnit.unitStr(context),
            image = ConditionIcon.pressureImage(pressureHpa)
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

        val sunImage = ConditionIcon.sunTrackImage(
            sunriseMillis = sunriseMillis,
            sunsetMillis = sunsetMillis,
            System.currentTimeMillis()
        )
        return SunItem(
            sunriseValueStr = timeMillisToStr(context, sunriseMillis),
            sunsetValueStr = timeMillisToStr(context, sunsetMillis),
            image = sunImage
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
                val probStr = if (prob >= MIN_PROBABILITY_PRECIP_DISPLAY) "$prob%" else ""

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
                val probStr = if (prob >= MIN_PROBABILITY_PRECIP_DISPLAY) "$prob%" else ""

                DailyItem(dateStr, iconId, tempStr, probStr)
            }
    }

    fun detailsRain(
        context: Context,
        weather: WeatherData,
        rainUnit: RainUnit
    ): DetailsItem {
        val barData = weather.hourlyTimeMillis.indices
            .drop(weather.nextHourlyIndex().coerceAtLeast(0))
            .mapNotNull { idx ->
                val rainMm = weather.hourlyRainMm.getOrNull(idx) ?: return@mapNotNull null
                val showersMm = weather.hourlyShowersMm.getOrNull(idx) ?: return@mapNotNull null
                val precipProb = weather.hourlyPrecipProb.getOrNull(idx) ?: return@mapNotNull null
                val tempCelsius = weather.hourlyTempCelsius.getOrNull(idx) ?: return@mapNotNull null
                val timeMillis = weather.hourlyTimeMillis[idx]

                val rainVal = rainUnit.fromMm(rainMm + showersMm)

                val probStr = if (rainVal > 0.0 || tempCelsius > RAIN_MIN_TEMP_C) {
                    "${precipProb.toInt()}%"
                } else "0%"

                val valStr = if (rainVal > 0.0) {
                    String.format(Locale.getDefault(), rainUnit.format, rainVal)
                } else ""

                val timeStr = formatTime(context, timeMillis)
                val level = (rainVal / rainUnit.fromMm(RAIN_BAR_MAX_MM)).coerceAtMost(1.0)

                DataBarItem(
                    valStr = valStr,
                    probStr = probStr,
                    level = level,
                    fillColor = ConditionIcon.rainFillColor(level),
                    strokeColor = ConditionIcon.rainStrokeColor(),
                    timeStr = timeStr
                )
            }
        val rainMm = weather.todayRainMm()
        val showersMm = weather.todayShowersMm()
        val valStr = (rainMm?.plus(showersMm ?: 0.0) ?: showersMm)?.let {
            String.format(Locale.getDefault(), rainUnit.format, rainUnit.fromMm(it))
        }
        return DetailsItem(valStr, rainUnit.unitStr(context), barData)
    }

    fun detailsSnow(
        context: Context,
        weather: WeatherData,
        snowUnit: SnowUnit
    ): DetailsItem {
        val barData = weather.hourlyTimeMillis.indices
            .drop(weather.nextHourlyIndex().coerceAtLeast(0))
            .mapNotNull { idx ->
                val snowCm = weather.hourlySnowCm.getOrNull(idx) ?: return@mapNotNull null
                val precipProb = weather.hourlyPrecipProb.getOrNull(idx) ?: return@mapNotNull null
                val tempCelsius = weather.hourlyTempCelsius.getOrNull(idx) ?: return@mapNotNull null
                val timeMillis = weather.hourlyTimeMillis[idx]

                val snow = snowUnit.fromCm(snowCm)

                val probStr = if (snow > 0.0 || tempCelsius < SNOW_MAX_TEMP_C) {
                    "${precipProb.toInt()}%"
                } else "0%"

                val valStr = if (snow > 0.0) {
                    String.format(Locale.getDefault(), snowUnit.format, snow)
                } else ""

                val timeStr = formatTime(context, timeMillis)
                val level = (snow / snowUnit.fromCm(SNOW_BAR_MAX_CM)).coerceAtMost(1.0)

                DataBarItem(
                    valStr = valStr,
                    probStr = probStr,
                    level = level,
                    fillColor = ConditionIcon.snowFillColor(),
                    strokeColor = ConditionIcon.snowStrokeColor(),
                    timeStr = timeStr
                )
            }
        val snowCm = weather.todaySnowCm()
        val valStr = snowCm?.let {
            String.format(Locale.getDefault(), snowUnit.format, snowUnit.fromCm(it))
        }
        return DetailsItem(valStr, snowUnit.unitStr(context), barData)
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

                DataBarItem(
                    probStr = probStr,
                    level = level,
                    fillColor = ConditionIcon.humidityFillColor(level),
                    strokeColor = ConditionIcon.humidityStrokeColor(),
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
                val uvIndex = weather.hourlyUvIndex.getOrNull(idx) ?: return@mapNotNull null
                val timeMillis = weather.hourlyTimeMillis[idx]

                val valStr = "${uvIndex.toInt()}"
                val timeStr = formatTime(context, timeMillis)
                val level = (uvIndex / 11).coerceAtMost(1.0)

                DataBarItem(
                    valStr = valStr,
                    level = level,
                    fillColor = ConditionIcon.uvIndexFillColor(uvIndex),
                    strokeColor = ConditionIcon.uvIndexStrokeColor(uvIndex),
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
        val tempValue = tempUnit.fromCelsius(tempCelsius)
        val unit = tempUnit.unitStr(context, fullUnit)
        return "${(tempValue + 0.5).toInt()}$unit"
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
