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

package com.kolakek.pimiwidget.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kolakek.pimiwidget.location.LocationData
import com.kolakek.pimiwidget.weather.WeatherData
import com.kolakek.pimiwidget.worker.StatusData

object DataRepository {

    private enum class DataKeys (val key: Preferences.Key<String>) {
        WEATHER(stringPreferencesKey("weather_json")),
        LOCATION(stringPreferencesKey("location_json")),
        STATUS(stringPreferencesKey("update_status_json"))
    }

    suspend fun deleteAllData(context: Context) {
        DataKeys.entries.forEach { JsonDataStore.delete(context, it.key) }
    }

    suspend fun storeWeatherData(context: Context, weatherData: WeatherData) {
        JsonDataStore.save(context, DataKeys.WEATHER.key, weatherData)
    }

    suspend fun loadWeatherData(context: Context): WeatherData? {
        return JsonDataStore.load<WeatherData>(context, DataKeys.WEATHER.key)
    }

    suspend fun storeLocationData(context: Context, locationData: LocationData) {
        JsonDataStore.save(context, DataKeys.LOCATION.key, locationData)
    }

    suspend fun loadLocationData(context: Context): LocationData? {
        return JsonDataStore.load<LocationData>(context, DataKeys.LOCATION.key)
    }

    suspend fun storeStatusData(context: Context, statusData: StatusData) {
        JsonDataStore.save(context, DataKeys.STATUS.key, statusData)
    }

    suspend fun loadStatusData(context: Context): StatusData? {
        return JsonDataStore.load<StatusData>(context, DataKeys.STATUS.key)
    }
}
