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

import com.kolakek.pimiwidget.location.LocationData
import kotlinx.coroutines.CancellationException
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import timber.log.Timber

object WeatherService {

    suspend fun getWeather(location: LocationData): WeatherData? {
        val url = weatherUrl(location, TIMEFORMAT_VALUE)

        Timber.d("getWeather: Get data for URL: $url")
        return try {
            val result = mapProviderData(HttpClientProvider.client.get(url).body())
            Timber.d("getWeather: Data retrieved")
            result
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.w("getWeather: Failed")
            null
        }
    }

    fun weatherUrl(location: LocationData, timeFormat: String): Url {
        return URLBuilder(BASE_URL).apply {
            parameters.apply {
                append(LATITUDE_KEY, location.lat.toString())
                append(LONGITUDE_KEY, location.long.toString())

                append(MINUTELY_KEY, MINUTELY_VALUE)
                append(DAILY_KEY, DAILY_VALUE)
                append(HOURLY_KEY, HOURLY_VALUE)

                append(FORECAST_MINUTES_KEY, FORECAST_MINUTES_VALUE)
                append(FORECAST_HOURS_KEY, FORECAST_HOURS_VALUE)
                append(FORECAST_DAYS_KEY, FORECAST_DAYS_VALUE)

                append(TIMEFORMAT_KEY, timeFormat)
                append(TIMEZONE_KEY, TIMEZONE_VALUE)
                append(TEMPUNIT_KEY, TEMPUNIT_VALUE)
            }
        }.build()
    }

    private fun mapProviderData(providerData: ProviderData): WeatherData? {
        val minutelyWeatherCode = try {
            providerData.minutely_15.weather_code.indices.map { i ->
                WeatherCodeMapper.getWeatherCode(
                    wmoCode = providerData.minutely_15.weather_code[i],
                    cloudCover = providerData.minutely_15.cloud_cover[i],
                    precipProb = providerData.minutely_15.precipitation_probability[i],
                    visibility = providerData.minutely_15.visibility[i],
                    cape = providerData.minutely_15.cape[i]
                ) ?: return null
            }
        } catch (_: ArrayIndexOutOfBoundsException) {
            Timber.w("mapProviderData: Minutely data index out of bounds")
            return null
        }
        val dailyWeatherCode = try {
            providerData.daily.weather_code.indices.map { i ->
                WeatherCodeMapper.getWeatherCode(
                    wmoCode = providerData.daily.weather_code[i],
                    cloudCover = providerData.daily.cloud_cover_mean[i],
                    precipProb = providerData.daily.precipitation_probability_max[i],
                    visibility = providerData.daily.visibility_mean[i],
                    cape = providerData.daily.cape_max[i]
                ) ?: return null
            }
        } catch (_: ArrayIndexOutOfBoundsException) {
            Timber.w("mapProviderData: Daily data index out of bounds")
            return null
        }
        val hourlyWarningCode = try {
            providerData.hourly.weather_code.indices.map { i ->
                WarningCodeMapper.getWarningCode(
                    uvIndex = providerData.hourly.uv_index[i],
                    uvIndexClearSky = providerData.hourly.uv_index_clear_sky[i],
                    cloudCover = providerData.hourly.cloud_cover[i],
                    apparentTempCelsius = providerData.hourly.apparent_temperature[i]
                )
            }
        } catch (_: ArrayIndexOutOfBoundsException) {
            Timber.w("mapProviderData: Hourly data index out of bounds")
            return null
        }
        return WeatherData(
            minutelyWeatherCode = minutelyWeatherCode,
            minutelyTempCelsius = providerData.minutely_15.temperature_2m,
            minutelyIsDay = providerData.minutely_15.is_day.map { v -> v == 1 },
            minutelyTimeMillis = providerData.minutely_15.time.map { v -> v * 1000L },
            hourlyTempCelsius = providerData.hourly.temperature_2m,
            hourlyIsDay = providerData.hourly.is_day.map { v -> v == 1 },
            hourlyWarningCode = hourlyWarningCode,
            hourlyTimeMillis = providerData.hourly.time.map { v -> v * 1000L },
            dailyWeatherCode = dailyWeatherCode,
            dailyTempMinCelsius = providerData.daily.temperature_2m_min,
            dailyTempMaxCelsius = providerData.daily.temperature_2m_max,
            dailyTimeMillis = providerData.daily.time.map { v -> v * 1000L }
        )
    }
}