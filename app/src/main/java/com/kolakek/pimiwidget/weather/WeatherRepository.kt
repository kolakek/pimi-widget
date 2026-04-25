package com.kolakek.pimiwidget.weather

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import timber.log.Timber

object WeatherRepository {

    suspend fun saveWeather(context: Context, weatherData: WeatherData) {
        val jsonString = Json.encodeToString(weatherData)

        context.dataStore.edit { prefs ->
            prefs[WEATHER_KEY] = jsonString
        }
    }

    fun loadWeatherSync(context: Context): WeatherData? {
        val prefs = runBlocking {
            context.dataStore.data.first()
        }

        val json = prefs[WEATHER_KEY] ?: return null

        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            Timber.e(e, "loadWeatherSync(): Failed to decode weather.")
            null
        }
    }
}
