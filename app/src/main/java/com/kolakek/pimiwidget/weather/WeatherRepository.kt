package com.kolakek.pimiwidget.weather

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.serialization.json.Json

object WeatherRepository {

    suspend fun saveWeather(context: Context, weatherData: WeatherData) {
        val jsonString = Json.encodeToString(weatherData)

        context.dataStore.edit { prefs ->
            prefs[WEATHER_KEY] = jsonString
        }
    }
}
