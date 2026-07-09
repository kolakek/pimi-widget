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

package com.kolakek.pimiwidget.utility

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.kolakek.pimiwidget.BuildConfig
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.widget.WidgetUpdater
import com.kolakek.pimiwidget.worker.WorkManagerHelper

object MigrateApp {

    fun migrate(context: Context) {
        val previousVersionCode = getPreviousVersionCode(context)

        if (previousVersionCode < 21) setFirstAvailableWeatherApp(context)

        if (previousVersionCode < 22) {
            PreferencesHelper.setTempUnitPreference(context, PreferencesHelper.TempUnitPref.AUTO)
            PreferencesHelper.setTextColorPreference(context, PreferencesHelper.ColorPref.AUTO)
            PreferencesHelper.setIconColorPreference(context, PreferencesHelper.ColorPref.AUTO)
            PreferencesHelper.setAlarmPreference(context, true)
        }
        storeCurrentVersionCode(context)

        WidgetUpdater.updateWidgets(context)
        WorkManagerHelper.enqueueWork(context, workPolicy = ExistingPeriodicWorkPolicy.UPDATE)
    }

    private fun setFirstAvailableWeatherApp(context: Context) {
        WeatherApp.entries.firstOrNull { app ->
            AppLookup.isAppInstalled(context, app.packageName)
        }?.let { PreferencesHelper.setWeatherApp(context, it) }
    }

    private fun getPreviousVersionCode(context: Context): Long {
        val prefs = context.getSharedPreferences(KEY_APP_PREFS, Context.MODE_PRIVATE)

        return if (
            prefs.contains(KEY_VERSION_CODE)
        ) {
            prefs.getLong(KEY_VERSION_CODE, 22)
        } else if (
            PreferenceManager.getDefaultSharedPreferences(context).contains(KEY_PREF_V21)
        ) {
            21
        } else {
            20
        }
    }

    private fun storeCurrentVersionCode(context: Context) {
        val prefs = context.getSharedPreferences(KEY_APP_PREFS, Context.MODE_PRIVATE)
        prefs.edit { putLong(KEY_VERSION_CODE, BuildConfig.VERSION_CODE.toLong()) }
    }
}
