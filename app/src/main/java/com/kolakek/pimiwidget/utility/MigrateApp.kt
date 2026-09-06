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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import com.kolakek.pimiwidget.BuildConfig
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.worker.UpdateAction
import com.kolakek.pimiwidget.worker.WorkManagerHelper

object MigrateApp {

    fun migrate(context: Context) {
        val previousVersionCode = getPreviousVersionCode(context)

        if (previousVersionCode < 22) {
            PreferencesHelper.setTempUnitPreference(
                context,
                PreferencesHelper.TempUnitPref.AUTO
            )
            PreferencesHelper.setTextColorPreference(
                context,
                PreferencesHelper.ColorPref.AUTO
            )
            PreferencesHelper.setIconColorPreference(
                context,
                PreferencesHelper.ColorPref.AUTO
            )
            PreferencesHelper.setWidgetStylePreference(
                context,
                PreferencesHelper.WidgetStylePref.CLASSIC
            )
            PreferencesHelper.setAuxDisplayPreference(
                context,
                PreferencesHelper.AuxDisplayPref.NOTHING
            )
            PreferencesHelper.setLocationFallbackPreference(
                context,
                true
            )
        }
        if (previousVersionCode < 23) {
            val widgetStyleSolid = PreferencesHelper.WidgetStylePref.SOLID
            val auxDisplayPlace = PreferencesHelper.AuxDisplayPref.PLACE_CONDITION
            val auxDisplayNothing = PreferencesHelper.AuxDisplayPref.NOTHING

            if (PreferencesHelper.getWidgetStylePreference(context) == widgetStyleSolid) {
                PreferencesHelper.setAuxDisplayPreference(context, auxDisplayPlace)
            } else {
                PreferencesHelper.setAuxDisplayPreference(context, auxDisplayNothing)
            }
        }
        storeCurrentVersionCode(context)

        WorkManagerHelper.enqueueOneTimeWork(
            context,
            UpdateAction.WEATHER_FETCH_THEN_REFRESH,
            ExistingWorkPolicy.REPLACE
        )
        WorkManagerHelper.enqueuePeriodicWork(
            context,
            workPolicy = ExistingPeriodicWorkPolicy.UPDATE
        )
    }

    fun storeCurrentVersionCode(context: Context) {
        val prefs = context.getSharedPreferences(KEY_PIMI_PREFERENCES, Context.MODE_PRIVATE)
        prefs.edit { putLong(KEY_VERSION_CODE, BuildConfig.VERSION_CODE.toLong()) }
    }

    private fun getPreviousVersionCode(context: Context): Long {
        val prefs = context.getSharedPreferences(KEY_PIMI_PREFERENCES, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_VERSION_CODE, Long.MAX_VALUE)
    }
}
