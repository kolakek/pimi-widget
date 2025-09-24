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

package com.kolakek.pimiwidget.widget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.work.ExistingPeriodicWorkPolicy
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.PimiData
import com.kolakek.pimiwidget.worker.WidgetUpdater

class WidgetSettingsFragment : PreferenceFragmentCompat() {

    private val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION
    private val backgroundLocationPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) requestNextPermission(preferenceManager.context) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pimi_widget_prefs, rootKey)

        preferenceManager.findPreference<Preference>(KEY_UPDATE_BUTTON)
            ?.setOnPreferenceClickListener {
                (activity as? WidgetConfigureActivity)?.finishWidgetConfigureActivity()
                true
            }

        val context = preferenceManager.context
        val weatherSwitch: SwitchPreferenceCompat? = findPreference(KEY_WEATHER_SWITCH)
        val debugField: Preference? = findPreference(KEY_UPDATE_INFO)

        if (WidgetUpdater.permissionsDenied(context) && weatherSwitch?.isChecked == true) {
            weatherSwitch.isChecked = false
            WidgetUpdater.cancelPeriodicWorker(context)
        }

        PimiData.timeMillis?.let {
            debugField?.setSummary(
                getString(
                    R.string.config_update_descr,
                    ageString(it),
                    if (PimiData.locationSuccess) getString(R.string.location_symbol) else "",
                    if (PimiData.weatherSuccess) getString(R.string.weather_symbol) else ""
                )
            )
        }
        debugField?.setOnPreferenceClickListener {
            showDebugDialog(context)
            true
        }

        weatherSwitch?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true && WidgetUpdater.permissionsDenied(context)) {
                requestNextPermission(context)
                false
            } else if (newValue == true) {
                WidgetUpdater.enqueuePeriodicWorker(
                    context,
                    0,
                    ExistingPeriodicWorkPolicy.KEEP
                )
                true
            } else {
                WidgetUpdater.cancelPeriodicWorker(context)
                true
            }
        }
    }

    private fun requestNextPermission(context: Context) {
        val permission: String
        val title: String
        val message: String

        if (context.checkSelfPermission(coarseLocationPermission) == PackageManager
                .PERMISSION_DENIED
        ) {
            permission = coarseLocationPermission
            title = getString(R.string.config_loc_perm_alert_title)
            message = getString(R.string.config_loc_perm_alert)

        } else if (context.checkSelfPermission(backgroundLocationPermission) == PackageManager
                .PERMISSION_DENIED
        ) {
            permission = backgroundLocationPermission
            title = getString(R.string.config_bg_perm_alert_title)
            message = getString(
                R.string.config_bg_perm_alert,
                context.packageManager.backgroundPermissionOptionLabel
            )

        } else if (context.checkSelfPermission(backgroundLocationPermission) == PackageManager
                .PERMISSION_GRANTED
        ) {
            findPreference<SwitchPreferenceCompat>(KEY_WEATHER_SWITCH)?.apply {
                isChecked = true
                WidgetUpdater.enqueuePeriodicWorker(
                    context,
                    0,
                    ExistingPeriodicWorkPolicy.KEEP
                )
            }
            return
        } else {
            return
        }
        if (shouldShowRequestPermissionRationale(permission)) {
            showRationaleDialog(context, permission, title, message)
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun showDebugDialog(
        context: Context
    ) {
        val builder = AlertDialog.Builder(context)

        builder.setMessage(
            getString(R.string.config_alert_debug_worker) +
                    ": ${WidgetUpdater.getWorkerStatus(context)}"
        )
        builder.setTitle(R.string.config_alert_debug_title)
        builder.setCancelable(true)
        builder.setPositiveButton(
            getString(R.string.config_alert_button_ok)
        ) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showRationaleDialog(
        context: Context,
        permission: String,
        title: String,
        message: String
    ) {
        val builder = AlertDialog.Builder(context)

        builder.setMessage(message)
        builder.setTitle(title)
        builder.setCancelable(false)
        builder.setPositiveButton(
            getString(R.string.config_alert_button_ok)
        ) { dialog, _ ->
            requestPermissionLauncher.launch(permission)
            dialog.dismiss()
        }
        builder.setNegativeButton(
            getString(R.string.config_alert_button_stop)
        ) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun ageString(timeMillis: Long): String {
        val ageMins: Int = ((System.currentTimeMillis() - timeMillis) / 1000L / 60L).toInt()
        val ageHours = ageMins / 60
        val ageDays = ageHours / 24
        return when {
            ageMins < 120 -> resources.getQuantityString(R.plurals.minutes, ageMins, ageMins)
            ageMins < 60 * 48 -> resources.getQuantityString(R.plurals.hours, ageHours, ageHours)
            else -> resources.getQuantityString(R.plurals.days, ageDays, ageDays)
        }
    }
}
