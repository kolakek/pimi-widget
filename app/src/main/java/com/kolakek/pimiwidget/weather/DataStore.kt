package com.kolakek.pimiwidget.weather

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "weather_prefs")

val WEATHER_KEY = stringPreferencesKey("weather_json")
