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

import org.junit.Assert.assertEquals
import org.junit.Test

class GeocodingServiceTest {

    @Test
    fun placeRegion_joinsRegionAndCountry() {
        assertEquals("Ile-de-France, France", GeocodingService.placeRegion("Paris", "Ile-de-France", "France"))
    }

    @Test
    fun placeRegion_dropsPartsRepeatingTheName() {
        assertEquals("Germany", GeocodingService.placeRegion("Berlin", "Berlin", "Germany"))
        assertEquals("", GeocodingService.placeRegion("Monaco", null, "Monaco"))
    }

    @Test
    fun placeRegion_dropsBlankParts() {
        assertEquals("", GeocodingService.placeRegion("Oslo", null, "  "))
    }

    @Test
    fun placeRegion_dropsDuplicateRegionAndCountry() {
        assertEquals("Luxembourg", GeocodingService.placeRegion("Luxembourg City", "Luxembourg", "Luxembourg"))
    }

    @Test
    fun placeRegion_trimsParts() {
        assertEquals("Bavaria, Germany", GeocodingService.placeRegion("Munich", " Bavaria ", "Germany "))
        assertEquals("Germany", GeocodingService.placeRegion(" Berlin ", "Berlin", "Germany"))
    }
}
