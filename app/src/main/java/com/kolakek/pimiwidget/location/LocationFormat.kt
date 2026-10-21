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

import java.util.Locale

/**
 * Pure helpers for user entered locations. They have no Android dependencies so they
 * can be unit tested on the JVM.
 */
object LocationFormat {

    /**
     * Builds a display label such as "Berlin, Germany". Blank parts are dropped and
     * repeated parts are shown once (a search result can have the same city and
     * region name).
     */
    fun placeLabel(name: String, region: String?, country: String?): String =
        listOfNotNull(name, region, country)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString(", ")

    /** Label used when the user gives coordinates without a name, e.g. "52.52, 13.41". */
    fun coordinatesLabel(lat: Double, long: Double): String =
        String.format(Locale.US, "%.2f, %.2f", lat, long)

    /**
     * Parses a coordinate typed by the user. Accepts a comma as decimal separator.
     * Returns null when the text is not a finite number inside [min, max].
     */
    fun parseCoordinate(text: String, min: Double, max: Double): Double? {
        val value = text.trim().replace(',', '.').toDoubleOrNull() ?: return null
        return if (value.isFinite() && value in min..max) value else null
    }
}
