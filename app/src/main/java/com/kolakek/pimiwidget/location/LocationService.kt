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
import androidx.datastore.preferences.core.Preferences
import com.kolakek.pimiwidget.data.JsonDataStore
import com.kolakek.pimiwidget.exception.LocationUnavailableException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

object LocationService {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun fetchLocation(
        context: Context,
        dataKey: Preferences.Key<String>
    ): LocationData {
        Timber.d("getLocation: Get location")

        val locationManager = context.getSystemService(LocationManager::class.java)
        val lastLocation = locationManager.getLastKnownLocation(LOCATION_PROVIDER)

        val location = if (isLocationValid(lastLocation)) {
            Timber.d("getLocation: Use last location")
            lastLocation
        } else {
            Timber.d("getLocation: No valid last location, get current location")
            withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
                getCurrentLocation(locationManager, context)
            }
        }
        location ?: throw LocationUnavailableException("Failed to obtain location")

        val locationData = LocationData(location.latitude, location.longitude, location.time)

        storeLocationData(context, locationData, dataKey)
        return locationData
    }

    suspend fun getLocationData(
        context: Context,
        dataKey: Preferences.Key<String>
    ): LocationData? {
        return JsonDataStore.load<LocationData>(context, dataKey)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun getCurrentLocation(
        locationManager: LocationManager,
        context: Context
    ): Location? = suspendCancellableCoroutine { cont ->

        val cancellationSignal = CancellationSignal()
        val executor = ContextCompat.getMainExecutor(context)

        locationManager.getCurrentLocation(
            LOCATION_PROVIDER,
            cancellationSignal,
            executor
        ) { location ->
            if (cont.isActive) cont.resumeWith(Result.success(location))
        }
        cont.invokeOnCancellation {
            cancellationSignal.cancel()
        }
    }

    private suspend fun storeLocationData(
        context: Context,
        locationData: LocationData,
        dataKey: Preferences.Key<String>
    ) {
        JsonDataStore.save(context, dataKey, locationData)
    }

    private fun isLocationValid(location: Location?): Boolean =
        location != null && (System.currentTimeMillis() - location.time <= LOCATION_MAX_AGE_MILLIS)
}
