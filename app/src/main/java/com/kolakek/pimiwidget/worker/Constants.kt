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

internal const val WORK_NAME = "periodic-update-kolakek-pimi-widget"
internal const val WORK_MODE_KEY = "work_mode"
internal const val STATUS_STRING_RUNNING = "Running"
internal const val DATA_MAX_AGE_MILLIS = 60 * 60 * 1000L
internal const val WORK_INTERVAL_MILLIS = 30 * 60 * 1000L
internal const val RECOVERY_INTERVAL_MILLIS = 15 * 60 * 1000L
internal const val BACKOFF_DELAY_MILLIS = 30 * 1000L
internal const val MAX_NUM_RETRIES = 3
