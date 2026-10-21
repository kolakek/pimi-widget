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
import org.junit.Assert.assertNull
import org.junit.Test

class LocationFormatTest {

    @Test
    fun placeLabel_joinsNameRegionAndCountry() {
        assertEquals("Paris, Ile-de-France, France", LocationFormat.placeLabel("Paris", "Ile-de-France", "France"))
    }

    @Test
    fun placeLabel_dropsRepeatedAndBlankParts() {
        assertEquals("Berlin, Germany", LocationFormat.placeLabel("Berlin", "Berlin", "Germany"))
        assertEquals("Oslo", LocationFormat.placeLabel("Oslo", null, "  "))
    }

    @Test
    fun coordinatesLabel_usesDotAndTwoDecimals() {
        assertEquals("52.52, 13.41", LocationFormat.coordinatesLabel(52.5244, 13.4105))
        assertEquals("-33.87, 151.21", LocationFormat.coordinatesLabel(-33.8688, 151.2093))
    }

    @Test
    fun parseCoordinate_acceptsDotAndCommaDecimals() {
        assertEquals(52.52, LocationFormat.parseCoordinate("52.52", -90.0, 90.0)!!, 0.0)
        assertEquals(52.52, LocationFormat.parseCoordinate(" 52,52 ", -90.0, 90.0)!!, 0.0)
        assertEquals(-13.4, LocationFormat.parseCoordinate("-13.4", -180.0, 180.0)!!, 0.0)
    }

    @Test
    fun parseCoordinate_acceptsTheLimits() {
        assertEquals(90.0, LocationFormat.parseCoordinate("90", -90.0, 90.0)!!, 0.0)
        assertEquals(-180.0, LocationFormat.parseCoordinate("-180", -180.0, 180.0)!!, 0.0)
    }

    @Test
    fun parseCoordinate_rejectsInvalidInput() {
        assertNull(LocationFormat.parseCoordinate("", -90.0, 90.0))
        assertNull(LocationFormat.parseCoordinate("abc", -90.0, 90.0))
        assertNull(LocationFormat.parseCoordinate("90.01", -90.0, 90.0))
        assertNull(LocationFormat.parseCoordinate("-180.5", -180.0, 180.0))
        assertNull(LocationFormat.parseCoordinate("NaN", -90.0, 90.0))
        assertNull(LocationFormat.parseCoordinate("Infinity", -90.0, 90.0))
    }
}
