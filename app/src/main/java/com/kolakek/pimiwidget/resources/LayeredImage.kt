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

package com.kolakek.pimiwidget.resources

data class LayeredImage(
    val backResId: Int = 0,
    val fillResId: Int = 0,
    val maskResId: Int = 0,
    val iconResId: Int = 0,
    val fillColor: Int = 0,
    val scaleFactor: Float = 0f,
    val rotation: Float = 0f,
    val height: Float = 0f,
    val posX: Float = 0f,
    val posY: Float = 0f
)
