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
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.kolakek.pimiwidget.R

class PermissionHandler(caller: ActivityResultCaller, private val activity: () -> Activity) {

    private var pendingPermission: String? = null
    private var pendingFallbackMessage: String? = null
    private var onPermissionGranted: (() -> Unit)? = null

    private val launcher = caller.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val permission = pendingPermission ?: return@registerForActivityResult
        val blocked = !ActivityCompat.shouldShowRequestPermissionRationale(activity(), permission)
        if (granted) onPermissionGranted?.invoke() else if (blocked) showSettingsDialog()
    }

    fun askPermission(
        permission: String,
        title: String,
        message: String,
        fallbackMessage: String,
        onGranted: () -> Unit
    ) {
        pendingPermission = permission
        pendingFallbackMessage = fallbackMessage
        onPermissionGranted = onGranted

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
            .setPositiveButton(R.string.config_alert_button_continue) { _, _ ->
                launcher.launch(permission)
            }
            .setNegativeButton(R.string.config_alert_button_cancel, null)
            .show()
    }

    private fun showSettingsDialog() {
        val activity = activity()
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        AlertDialog.Builder(activity)
            .setMessage(pendingFallbackMessage)
            .setTitle(R.string.config_perm_settings_title)
            .setNeutralButton(R.string.config_open_settings_button) { _, _ ->
                activity.startActivitySafely(intent)
            }
            .setNegativeButton(R.string.config_alert_button_cancel, null)
            .show()
    }
}
