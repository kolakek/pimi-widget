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
import androidx.datastore.preferences.core.Preferences
import com.kolakek.pimiwidget.data.JsonDataStore
import com.kolakek.pimiwidget.exception.WeatherMappingException
import com.kolakek.pimiwidget.location.LocationData
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import timber.log.Timber

object WeatherService {

    suspend fun fetchWeatherData(
        context: Context,
        location: LocationData,
        dataKey: Preferences.Key<String>
    ): WeatherData {
        val url = weatherUrl(location, TIMEFORMAT_VALUE)

        Timber.d("getWeather: Get data for URL: $url")

        val providerData = HttpClientProvider.client.get(url).body<ProviderData>()
        val weatherData = mapProviderData(providerData)

        storeWeatherData(context, weatherData, dataKey)
        return weatherData
    }

    suspend fun getWeatherData(
        context: Context,
        dataKey: Preferences.Key<String>
    ): WeatherData? {
        return JsonDataStore.load<WeatherData>(context, dataKey)
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

                append(TEMP_UNIT_KEY, TEMP_UNIT_VALUE)
                append(PRECIP_UNIT_KEY, PRECIP_UNIT_VALUE)
                append(WIND_SPEED_UNIT_KEY, WIND_SPEED_UNIT_VALUE)
            }
        }.build()
    }

    private fun mapProviderData(providerData: ProviderData): WeatherData {
        val minutelyWeatherCode = providerData.minutely_15.weather_code.indices.map { i ->
            WeatherCodeMapper.getWeatherCode(
                wmoCode = providerData.minutely_15.weather_code[i].toInt(),
                cloudCover = providerData.minutely_15.cloud_cover[i],
                precipProb = providerData.minutely_15.precipitation_probability[i],
                visibility = providerData.minutely_15.visibility[i],
                cape = providerData.minutely_15.cape[i]
            ) ?: throw WeatherMappingException("Failed to map minutely data")
        }
        val dailyWeatherCode = providerData.daily.weather_code.indices.map { i ->
            WeatherCodeMapper.getWeatherCode(
                wmoCode = providerData.daily.weather_code[i].toInt(),
                cloudCover = providerData.daily.cloud_cover_mean[i],
                precipProb = providerData.daily.precipitation_probability_max[i],
                visibility = providerData.daily.visibility_mean[i],
                cape = providerData.daily.cape_max[i]
            ) ?: throw WeatherMappingException("Failed to map daily data")
        }
        val hourlyWarningCode = providerData.hourly.weather_code.indices.map { i ->
            WarningCodeMapper.getWarningCode(
                wmoCode = providerData.hourly.weather_code[i].toInt(),
                uvIndex = providerData.hourly.uv_index[i].toInt(),
                uvIndexClearSky = providerData.hourly.uv_index_clear_sky[i].toInt(),
                cloudCover = providerData.hourly.cloud_cover[i],
                apparentTempCelsius = providerData.hourly.apparent_temperature[i],
                rain = providerData.hourly.rain[i] + providerData.hourly.showers[i],
                rainProb = providerData.hourly.precipitation_probability[i],
                cape = providerData.hourly.cape[i],
                windGusts = providerData.hourly.wind_gusts_10m[i]
            )
        }
        return WeatherData(
            minutelyWeatherCode = minutelyWeatherCode,
            minutelyTempCelsius = providerData.minutely_15.temperature_2m,
            minutelyIsDay = providerData.minutely_15.is_day.map { v -> v.toInt() == 1 },
            minutelyTimeMillis = providerData.minutely_15.time.map { v -> v.toLong() * 1000L },
            hourlyTempCelsius = providerData.hourly.temperature_2m,
            hourlyIsDay = providerData.hourly.is_day.map { v -> v.toInt() == 1 },
            hourlyWarningCode = hourlyWarningCode,
            hourlyTimeMillis = providerData.hourly.time.map { v -> v.toLong() * 1000L },
            dailyWeatherCode = dailyWeatherCode,
            dailyTempMinCelsius = providerData.daily.temperature_2m_min,
            dailyTempMaxCelsius = providerData.daily.temperature_2m_max,
            dailyTimeMillis = providerData.daily.time.map { v -> v.toLong() * 1000L },
            timeMillis = System.currentTimeMillis()
        )
    }

    private suspend fun storeWeatherData(
        context: Context,
        weatherData: WeatherData,
        dataKey: Preferences.Key<String>
    ) {
        JsonDataStore.save(context, dataKey, weatherData)
    }
}
