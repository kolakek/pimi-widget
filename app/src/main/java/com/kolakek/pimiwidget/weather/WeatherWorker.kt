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

import com.kolakek.pimiwidget.weather.WeatherData
import com.kolakek.pimiwidget.data.LocationData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber

class WeatherWorker {

    companion object {

        suspend fun getWeather(location: LocationData?): WeatherData? {
            Timber.d("getWeather(): Begin function.")

            if (location == null) {
                Timber.d("getWeather(): Return null.")
                return null
            }
            Timber.d("getWeather(): Instantiate client.")
            val client = HttpClient(CIO) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(
                        Json {
                            isLenient = true
                            ignoreUnknownKeys = true
                        }
                    )
                }
            }
            var providerData: ProviderData?

            val str = "$URL?latitude=${location.lat}&longitude=${location.long}&$DAT1&$DAT2&$OPTS"
            Timber.d("getWeather(): $str")

            try {
                Timber.d("getWeather(): Get weather data.")
                providerData = client.get(str).body()
            } catch (_: Throwable) {
                Timber.w("getWeather(): Catch exception.")
                providerData = null
            } finally {
                Timber.d("getWeather(): Close client.")
                client.close()
            }
            Timber.d("getWeather(): Store data.")
            val weatherData = providerData?.let {
                WeatherData(
                    it.hourly.temperature_2m,
                    it.hourly.weather_code,
                    it.hourly.time.map { v -> v * 1000L },
                    it.hourly.is_day,
                    it.daily.temperature_2m_min,
                    it.daily.temperature_2m_max,
                    it.daily.rain_sum,
                    it.daily.showers_sum,
                    it.daily.snowfall_sum,
                    it.daily.visibility_mean,
                    it.daily.cloud_cover_mean,
                    it.daily.weather_code,
                    it.daily.time.map { v -> v * 1000L }
                )
            }
            Timber.d("getWeather(): End function.")
            return weatherData
        }
    }
}
