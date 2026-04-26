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
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber

object LocationService {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getLocation(context: Context): LocationData? {
        Timber.d("getLocation: Get location")

        val locationManager = context.getSystemService(LocationManager::class.java)

        val lastLocation = try {
            locationManager.getLastKnownLocation(LOCATION_PROVIDER)
        } catch (e: SecurityException) {
            Timber.e(e, "getLocation: Missing location permission")
            null
        }
        val location = if (
            lastLocation == null ||
            System.currentTimeMillis() - lastLocation.time > LOCATION_MAX_AGE_MILLIS
        ) {
            Timber.d("getLocation: No valid last location, get current location")
            getCurrentLocation(locationManager, LOCATION_PROVIDER, context)
        } else {
            lastLocation
        }
        if (location == null) {
            Timber.w("getLocation: Failed to determine location")
        } else {
            Timber.d("getLocation: Location obtained successfully")
        }
        return location?.let { LocationData(it.latitude, it.longitude, it.time) }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getCurrentLocation(
        manager: LocationManager,
        provider: String,
        context: Context
    ): Location? = suspendCancellableCoroutine { cont ->
        val cancellationSignal = CancellationSignal()
        val executor = ContextCompat.getMainExecutor(context)

        try {
            manager.getCurrentLocation(provider, cancellationSignal, executor) { location ->
                cont.resume(location) { _, _, _ -> }
            }
        } catch (e: SecurityException) {
            Timber.e(e, "getCurrentLocation: Missing location permission")
            cont.resume(null) { _, _, _ -> }
            return@suspendCancellableCoroutine
        }
        cont.invokeOnCancellation {
            cancellationSignal.cancel()
        }
    }
}
