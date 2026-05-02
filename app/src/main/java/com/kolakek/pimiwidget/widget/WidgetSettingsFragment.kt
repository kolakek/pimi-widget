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
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.work.ExistingPeriodicWorkPolicy
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.DataKeys
import com.kolakek.pimiwidget.data.JsonDataStore
import com.kolakek.pimiwidget.worker.UpdateStatusData
import com.kolakek.pimiwidget.worker.WorkManagerHelper
import java.util.Date

internal class WidgetSettingsFragment : PreferenceFragmentCompat() {

    private val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION
    private val backgroundLocationPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) requestNextPermission(preferenceManager.context) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pimi_widget_prefs, rootKey)

        val context = preferenceManager.context
        val weatherSwitch: SwitchPreferenceCompat? = findPreference(KEY_WEATHER_SWITCH)
        val debugField: Preference? = findPreference(KEY_DEBUG_INFO)
        val sourceCodeField: Preference? = findPreference(KEY_SOURCE_CODE)

        if (permissionsDenied(context) && weatherSwitch?.isChecked == true) {
            weatherSwitch.isChecked = false
            WorkManagerHelper.cancelWorkers(context)
        }

        debugField?.setOnPreferenceClickListener {
            showDebugDialog(context, weatherSwitch?.isChecked)
            true
        }

        sourceCodeField?.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, SOURCE_CODE_URL.toUri()))
            true
        }

        weatherSwitch?.setOnPreferenceChangeListener { _, newValue ->
            when (newValue) {
                true if permissionsDenied(context) -> {
                    requestNextPermission(context)
                    false
                }
                true -> {
                    WorkManagerHelper.enqueuePeriodicWorker(
                        context,
                        0,
                        ExistingPeriodicWorkPolicy.KEEP
                    )
                    true
                }
                else -> {
                    WorkManagerHelper.cancelWorkers(context)
                    true
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView.isVerticalScrollBarEnabled = false
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
                WorkManagerHelper.enqueuePeriodicWorker(
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

    private fun permissionsDenied(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_DENIED

    private fun showDebugDialog(
        context: Context,
        weatherEnabled: Boolean?
    ) {
        val builder = AlertDialog.Builder(context)
        val dataUpdateStatus: UpdateStatusData? = JsonDataStore.loadSync(
            context, DataKeys.UPDATE_STATUS_DATA_KEY
        )
        val updateStr = dataUpdateStatus?.lastUpdateTimeMillis?.let {
            "\n${getString(R.string.config_alert_debug_last_update, ageString(it))}\n"
        } ?: ""

        var workerStr = getString(R.string.config_alert_debug_worker) + " " +
                (WorkManagerHelper.getWorkerStatus(context)
                    ?: getString(R.string.config_alert_debug_na))

        WorkManagerHelper.getNextScheduleMillis(context)?.let {
            workerStr += " (${DateFormat.getTimeFormat(context).format(Date(it))})"
        }

        val locationStr = getString(R.string.config_alert_debug_location) +
                " ${statusString(dataUpdateStatus?.isLocationSuccess)}"

        val weatherStr = getString(R.string.config_alert_debug_weather) +
                " ${statusString(dataUpdateStatus?.isWeatherSuccess)}"

        builder.setMessage("$updateStr\n$locationStr\n\n$weatherStr\n\n$workerStr")
        builder.setTitle(R.string.config_alert_debug_title)
        builder.setCancelable(true)
        builder.setPositiveButton(
            getString(R.string.config_alert_button_close)
        ) { dialog, _ ->
            dialog.dismiss()
        }
        builder.apply {
            if (weatherEnabled == true) {
                setNegativeButton(getString(R.string.config_alert_button_update)) { dialog, _ ->
                    WorkManagerHelper.enqueueOneTimeWorker(context, forceUpdate = true)
                    dialog.dismiss()
                }
            }
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
            getString(R.string.config_alert_button_cancel)
        ) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun statusString(success: Boolean?): String {
        return when (success) {
            true -> getString(R.string.config_alert_debug_success)
            false -> getString(R.string.config_alert_debug_failed)
            null -> getString(R.string.config_alert_debug_na)
        }
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
