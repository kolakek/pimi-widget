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

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.data.SettingsRepository
import com.kolakek.pimiwidget.utility.KEY_PIMI_PREFERENCES
import com.kolakek.pimiwidget.utility.MigrateApp
import com.kolakek.pimiwidget.worker.WorkManagerHelper
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class PimiWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        WidgetUpdater.updateWidgets(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WorkManagerHelper.enqueuePeriodicWork(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { SettingsRepository.removeWidgetConfigured(context, it) }
    }

    override fun onDisabled(context: Context) {
        WorkManagerHelper.cancelPeriodicWork(context)
        PreferenceManager.getDefaultSharedPreferences(context).edit { clear() }
        context.getSharedPreferences(KEY_PIMI_PREFERENCES, Context.MODE_PRIVATE).edit { clear() }
        runBlocking { DataRepository.deleteAllData(context) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Timber.d("onReceive: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED ->
                WorkManagerHelper.enqueuePeriodicWork(
                    context,
                    initialDelayMillis = WORKER_INIT_DELAY_MILLIS,
                    workPolicy = ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
                )

            Intent.ACTION_LOCALE_CHANGED ->
                WidgetUpdater.updateWidgets(context)

            Intent.ACTION_MY_PACKAGE_REPLACED ->
                MigrateApp.migrate(context)

            AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED ->
                WidgetUpdater.refreshAlarms(context)

            ACTION_BIRTHDAY_DISMISS -> {
                BirthdayUpdater.updateDismissHash(context)
                WidgetUpdater.refreshBirthdays(context)
            }
        }
    }
}
