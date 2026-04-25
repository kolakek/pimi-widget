package com.kolakek.pimiwidget.weather

import kotlinx.serialization.Serializable

@Serializable
data class WeatherData (
    val hourlyTempCelsius: List<Double>,
    val hourlyWeatherCode: List<Int>,
    val hourlyTimeMillis: List<Long>,
    val hourlyIsDay: List<Int>,
    val dailyTempMinCelsius: List<Double>,
    val dailyTempMaxCelsius: List<Double>,
    val dailyRainSum: List<Double>,
    val dailyShowersSum: List<Double>,
    val dailySnowfallSum: List<Double>,
    val dailyVisibilityMean: List<Double>,
    val dailyCloudCoverMean: List<Int>,
    val dailyWeatherCode: List<Int>,
    val dailyTimeMillis: List<Long>
)