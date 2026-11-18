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
import androidx.core.content.edit
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kolakek.pimiwidget.BuildConfig
import com.kolakek.pimiwidget.birthday.BirthdayData
import com.kolakek.pimiwidget.location.LocationData
import com.kolakek.pimiwidget.weather.WeatherData
import com.kolakek.pimiwidget.worker.StatusData
import kotlinx.coroutines.flow.Flow

object DataRepository {

    private const val PREFS_NAME = "app_preferences"
    private const val KEY_VERSION_CODE = "version_code"
    private const val KEY_BIRTHDAY_DISMISS_HASH = "birthday_dismiss_hash"

    private enum class DataKeys (val key: Preferences.Key<String>) {
        WEATHER(stringPreferencesKey("weather_json")),
        LOCATION(stringPreferencesKey("location_json")),
        BIRTHDAYS(stringPreferencesKey("birthday_json")),
        STATUS(stringPreferencesKey("update_status_json"))
    }

    suspend fun deleteAllData(context: Context) {
        DataKeys.entries.forEach { JsonDataStore.delete(context, it.key) }
        prefs(context).edit { clear() }
    }

    suspend fun storeWeatherData(context: Context, weatherData: WeatherData) {
        JsonDataStore.save(context, DataKeys.WEATHER.key, weatherData)
    }

    suspend fun loadWeatherData(context: Context): WeatherData? {
        return JsonDataStore.load<WeatherData>(context, DataKeys.WEATHER.key)
    }

    suspend fun deleteWeatherData(context: Context) {
        JsonDataStore.delete(context, DataKeys.WEATHER.key)
    }

    fun observeWeatherData(context: Context): Flow<WeatherData?> {
        return JsonDataStore.observe(context, DataKeys.WEATHER.key)
    }

    suspend fun storeLocationData(context: Context, locationData: LocationData) {
        JsonDataStore.save(context, DataKeys.LOCATION.key, locationData)
    }

    suspend fun loadLocationData(context: Context): LocationData? {
        return JsonDataStore.load<LocationData>(context, DataKeys.LOCATION.key)
    }

    suspend fun deleteLocationData(context: Context) {
        JsonDataStore.delete(context, DataKeys.LOCATION.key)
    }

    suspend fun storeStatusData(context: Context, statusData: StatusData) {
        JsonDataStore.save(context, DataKeys.STATUS.key, statusData)
    }

    suspend fun loadStatusData(context: Context): StatusData? {
        return JsonDataStore.load<StatusData>(context, DataKeys.STATUS.key)
    }

    suspend fun storeBirthdayData(context: Context, birthdayData: BirthdayData) {
        JsonDataStore.save(context, DataKeys.BIRTHDAYS.key, birthdayData)
    }

    suspend fun loadBirthdayData(context: Context): BirthdayData? {
        return JsonDataStore.load<BirthdayData>(context, DataKeys.BIRTHDAYS.key)
    }

    suspend fun deleteBirthdayData(context: Context) {
        JsonDataStore.delete(context, DataKeys.BIRTHDAYS.key)
        prefs(context).edit { remove(KEY_BIRTHDAY_DISMISS_HASH) }
    }

    fun storeVersionCode(context: Context) =
        prefs(context).edit { putLong(KEY_VERSION_CODE, BuildConfig.VERSION_CODE.toLong()) }

    fun loadVersionCode(context: Context) = prefs(context).getLong(KEY_VERSION_CODE, Long.MAX_VALUE)

    fun storeBirthdayDismissHash(context: Context, dismissHash: Int) =
        prefs(context).edit { putInt(KEY_BIRTHDAY_DISMISS_HASH, dismissHash) }

    fun loadBirthdayDismissHash(context: Context) =
        prefs(context).getInt(KEY_BIRTHDAY_DISMISS_HASH, 0)

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
