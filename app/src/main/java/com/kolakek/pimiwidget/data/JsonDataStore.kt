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
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import timber.log.Timber

object JsonDataStore {

    val json = Json {
        ignoreUnknownKeys = true
    }

    suspend inline fun <reified T> save(
        context: Context,
        key: Preferences.Key<String>,
        value: T
    ) {
        val jsonString = json.encodeToString(value)

        context.dataStore.edit { prefs ->
            prefs[key] = jsonString
        }
    }

    inline fun <reified T> loadSync(
        context: Context,
        key: Preferences.Key<String>
    ): T? {
        val prefs = runBlocking {
            context.dataStore.data.first()
        }

        val jsonString = prefs[key] ?: return null

        return try {
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            Timber.e(e, "loadSync: Failed to decode ${T::class.simpleName}")
            null
        }
    }
}
