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

package com.kolakek.pimiwidget.worker

internal const val PERIODIC_WIDGET_WORK_NAME = "periodic-update-kolakek-pimi-widget"
internal const val DATA_WORK_NAME = "one-time-update-kolakek-pimi-widget"
internal const val REFRESH_WIDGET_KEY = "refresh_widget"
internal const val BACKOFF_DELAY_MILLIS = 30 * 1000L
internal const val MAX_NUM_RETRIES = 5
internal const val WIDGET_UPDATE_INTERVAL_MILLIS = 30 * 60 * 1000L
internal const val STATUS_STRING_SUCCESS = "Succeeded"
internal const val STATUS_STRING_RUNNING = "Running"
