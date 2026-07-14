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
import android.os.Bundle
import androidx.work.ExistingPeriodicWorkPolicy
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.settings.PreferencesHelper
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

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        WidgetUpdater.refreshVisibility(context, appWidgetManager, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WorkManagerHelper.enqueuePeriodicWork(context)
    }

    override fun onDisabled(context: Context) {
        WorkManagerHelper.cancelPeriodicWork(context)
        PreferencesHelper.setWeatherPreference(context, false)
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
