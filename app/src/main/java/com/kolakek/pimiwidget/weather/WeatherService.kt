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
import com.kolakek.pimiwidget.weather.HttpClientProvider.client
import kotlinx.coroutines.CancellationException
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import timber.log.Timber

object WeatherService {

    suspend fun getWeather(location: LocationData): WeatherData? {
        val url = URLBuilder(BASE_URL).apply {
            parameters.append("latitude", location.lat.toString())
            parameters.append("longitude", location.long.toString())

            parameters.append(DAILY_KEY, DAILY_VALUE)
            parameters.append(HOURLY_KEY, HOURLY_VALUE)

            parameters.append(TIMEZONE_KEY, TIMEZONE_VALUE)
            parameters.append(FORECAST_DAYS_KEY, FORECAST_DAYS_VALUE)
            parameters.append(FORECAST_HOURS_KEY, FORECAST_HOURS_VALUE)
            parameters.append(TIMEFORMAT_KEY, TIMEFORMAT_VALUE)
        }.build()

        Timber.d("getWeather: Get data for URL: $url")
        return try {
            val result = mapProviderData(client.get(url).body())
            Timber.d("getWeather: Data successfully retrieved")
            result
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.w("getWeather: Failed")
            null
        }
    }

    private fun mapProviderData(providerData: ProviderData): WeatherData {
        return WeatherData(
            providerData.hourly.temperature_2m,
            providerData.hourly.weather_code,
            providerData.hourly.time.map { v -> v * 1000L },
            providerData.hourly.is_day,
            providerData.daily.temperature_2m_min,
            providerData.daily.temperature_2m_max,
            providerData.daily.rain_sum,
            providerData.daily.showers_sum,
            providerData.daily.snowfall_sum,
            providerData.daily.visibility_mean,
            providerData.daily.cloud_cover_mean,
            providerData.daily.weather_code,
            providerData.daily.time.map { v -> v * 1000L }
        )
    }
}
