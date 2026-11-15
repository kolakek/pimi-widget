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

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kolakek.pimiwidget.R

class PermissionHandler(private val fragment: Fragment) {

    private var onPermissionGranted: (() -> Unit)? = null

    private val requestPermissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { onPermissionGranted?.invoke() }

    fun isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

    fun isPermissionDenied(permission: String) =
        ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            permission
        ) == PackageManager.PERMISSION_DENIED

    fun askPermission(permission: String, title: String, message: String, onGranted: () -> Unit) {
        onPermissionGranted = { if (isPermissionGranted(permission)) onGranted() }
        if (fragment.shouldShowRequestPermissionRationale(permission)) {
            showRationaleDialog(permission, title, message)
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun showRationaleDialog(
        permission: String,
        title: String,
        message: String
    ) {
        AlertDialog.Builder(fragment.requireContext())
            .setMessage(message)
            .setTitle(title)
            .setCancelable(false)
            .setPositiveButton(R.string.config_alert_button_ok) { _, _ ->
                requestPermissionLauncher.launch(permission)
            }
            .setNegativeButton(R.string.config_alert_button_cancel, null)
            .show()
    }
}
