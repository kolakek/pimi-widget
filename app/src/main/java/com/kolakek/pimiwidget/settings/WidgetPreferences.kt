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

package com.kolakek.pimiwidget.settings

import com.kolakek.pimiwidget.resources.IconStyle
import com.kolakek.pimiwidget.widget.TempUnit
import com.kolakek.pimiwidget.widget.TextStyle

internal data class WidgetPreferences (
    val showWeather: Boolean,
    val showDailyForecast: Boolean,
    val showWeatherWarning: Boolean,
    val iconStyle: IconStyle,
    val textStyle: TextStyle,
    val tempUnit: TempUnit
)