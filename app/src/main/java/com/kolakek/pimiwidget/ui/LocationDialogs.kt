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

package com.kolakek.pimiwidget.ui

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.location.LocationService
import com.kolakek.pimiwidget.settings.startActivitySafely

fun Activity.showLocationServicesHint(onSkip: () -> Unit) {
    if (LocationService.isNetworkLocationAvailable(this)) {
        onSkip()
        return
    }
    AlertDialog.Builder(this)
        .setTitle(R.string.config_location_services_title)
        .setMessage(R.string.config_network_location_hint)
        .setPositiveButton(R.string.config_alert_button_skip) { _, _ -> onSkip() }
        .setNegativeButton(R.string.config_alert_button_cancel, null)
        .setNeutralButton(R.string.config_open_settings_button) { _, _ ->
            startActivitySafely(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        .show()
}
