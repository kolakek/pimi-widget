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

package com.kolakek.pimiwidget.location

import com.kolakek.pimiwidget.utility.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import kotlinx.serialization.Serializable
import java.util.Locale

/** A place found by a text search. */
data class GeocodingPlace(
    val label: String,
    val lat: Double,
    val long: Double
)

// The API omits empty fields, and omits "results" when nothing matches.
@Serializable
internal data class GeocodingResponse(
    val results: List<GeocodingResult> = emptyList()
)

@Serializable
internal data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val admin1: String? = null,
    val country: String? = null
)

/**
 * Looks up places by name with the Open-Meteo geocoding API, the same provider used
 * for the weather data. Only the typed text and the language code are sent.
 */
object GeocodingService {

    suspend fun search(
        query: String,
        language: String = Locale.getDefault().language
    ): List<GeocodingPlace> {
        val url = URLBuilder(GEOCODING_URL).apply {
            parameters.append("name", query.trim())
            parameters.append("count", GEOCODING_RESULT_COUNT.toString())
            parameters.append("language", language.ifBlank { "en" })
            parameters.append("format", "json")
        }.build()

        val response = HttpClientProvider.client.get(url).body<GeocodingResponse>()

        return response.results
            .filter {
                it.latitude in -MAX_LATITUDE..MAX_LATITUDE &&
                    it.longitude in -MAX_LONGITUDE..MAX_LONGITUDE
            }
            .map {
                GeocodingPlace(
                    label = LocationFormat.placeLabel(it.name, it.admin1, it.country),
                    lat = it.latitude,
                    long = it.longitude
                )
            }
    }
}
