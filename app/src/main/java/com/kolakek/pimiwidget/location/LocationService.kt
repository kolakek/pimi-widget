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

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.exception.LocationUnavailableException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

object LocationService {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun fetchLocation(
        context: Context
    ): LocationData {
        val locationManager = context.getSystemService(LocationManager::class.java)

        locationManager.isLocationEnabled

        getLastKnownLocation(locationManager)?.let {
            DataRepository.storeLocationData(context, it)
            return it
        }
        getCurrentLocation(locationManager, context)?.let {
            DataRepository.storeLocationData(context, it)
            return it
        }
        throw LocationUnavailableException("Failed to obtain location")
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLastKnownLocation(
        locationManager: LocationManager
    ): LocationData? = locationManager.getLastKnownLocation(LOCATION_PROVIDER)?.let {
        if (System.currentTimeMillis() - it.time <= LOCATION_MAX_AGE_MILLIS) {
            LocationData(it.latitude, it.longitude, it.time, CACHED_LOCATION_NAME)
        } else null
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun getCurrentLocation(
        locationManager: LocationManager,
        context: Context
    ): LocationData? = withTimeout(LOCATION_TIMEOUT_MILLIS) {
        val location = suspendCancellableCoroutine<Location?> { cont ->
            val cancellationSignal = CancellationSignal()

            locationManager.getCurrentLocation(
                LOCATION_PROVIDER,
                cancellationSignal,
                ContextCompat.getMainExecutor(context)
            ) { location ->
                cont.resume(location) { _, _, _ -> }
            }
            cont.invokeOnCancellation {
                cancellationSignal.cancel()
            }
        }
        location?.let {
            LocationData(it.latitude, it.longitude, it.time, FRESH_LOCATION_NAME)
        }
    }
}
