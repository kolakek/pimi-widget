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

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.app.Activity
import androidx.activity.result.ActivityResultCaller
import com.kolakek.pimiwidget.R

class LocationPermissionRequester(
    caller: ActivityResultCaller,
    private val activity: () -> Activity
) {

    private val permissionHandler = PermissionHandler(caller, activity)

    fun request(onGranted: () -> Unit) {
        val activity = activity()
        when {
            activity.isPermissionDenied(ACCESS_COARSE_LOCATION) ->
                permissionHandler.askPermission(
                    ACCESS_COARSE_LOCATION,
                    activity.getString(R.string.config_loc_perm_alert_title),
                    activity.getString(R.string.config_loc_perm_alert_message)
                ) { request(onGranted) }

            activity.isPermissionDenied(ACCESS_BACKGROUND_LOCATION) ->
                permissionHandler.askPermission(
                    ACCESS_BACKGROUND_LOCATION,
                    activity.getString(R.string.config_bg_perm_alert_title),
                    activity.getString(
                        R.string.config_bg_perm_alert_message,
                        activity.packageManager.backgroundPermissionOptionLabel
                    )
                ) { request(onGranted) }

            else -> onGranted()
        }
    }
}
