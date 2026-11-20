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

import android.app.Activity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.kolakek.pimiwidget.R

class PermissionHandler(caller: ActivityResultCaller, private val activity: () -> Activity) {

    private var onPermissionGranted: (() -> Unit)? = null

    private val launcher = caller.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { onPermissionGranted?.invoke() }

    fun askPermission(permission: String, title: String, message: String, onGranted: () -> Unit) {
        onPermissionGranted = { if (activity().isPermissionGranted(permission)) onGranted() }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity(), permission)) {
            showRationaleDialog(permission, title, message)
        } else {
            launcher.launch(permission)
        }
    }

    private fun showRationaleDialog(permission: String, title: String, message: String) {
        AlertDialog.Builder(activity())
            .setMessage(message)
            .setTitle(title)
            .setCancelable(false)
            .setPositiveButton(R.string.config_alert_button_ok) { _, _ ->
                launcher.launch(permission)
            }
            .setNegativeButton(R.string.config_alert_button_cancel, null)
            .show()
    }
}
