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
}
