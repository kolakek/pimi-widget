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
import com.kolakek.pimiwidget.data.LocationData
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber

object LocationWorker {

    private lateinit var locationManager: LocationManager

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getLocation(context: Context): LocationData? {
        Timber.d("getLocation(): Begin Function.")

        if (!::locationManager.isInitialized) {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        Timber.d("getLocation(): Get last location.")
        var location = locationManager.getLastKnownLocation(LOCATION_PROVIDER)
        location ?: Timber.d("getLocation(): Last location null.")

        if (location == null || location.elapsedRealtimeAgeMillis > LAST_LOCATION_MAX_AGE_MILLIS) {
            Timber.d("getLocation(): Get current location.")
            location = getCurrentLocation(locationManager, LOCATION_PROVIDER, context)
            location ?: Timber.d("getLocation(): Current location null.")
        }
        Timber.d("getLocation(): Store data.")
        val data = location?.let { LocationData(it.latitude, it.longitude, it.time) }

        Timber.d("getLocation(): End function.")
        return data
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getCurrentLocation(
        manager: LocationManager,
        provider: String,
        context: Context
    ): Location? = suspendCancellableCoroutine { cont ->

        val cancellationSignal = CancellationSignal()
        val executor = ContextCompat.getMainExecutor(context)

        manager.getCurrentLocation(provider, cancellationSignal, executor) { location ->
            cont.resume(location) { _, _, _ -> }
        }
        cont.invokeOnCancellation {
            cancellationSignal.cancel()
        }
    }
}
