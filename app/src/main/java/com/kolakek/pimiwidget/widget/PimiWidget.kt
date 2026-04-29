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

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import com.kolakek.pimiwidget.worker.WorkManagerHelper
import timber.log.Timber

class PimiWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            WidgetController.updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDisabled(context: Context) {
        Timber.d("onDisabled: Deactivate weather service, cancel workers")

        PreferencesHelper.setWeatherPreference(context, false)
        WorkManagerHelper.cancelWorkers(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Timber.d("onReceive: Enable worker, full widget update")
                WidgetController.updateAllWidgets(context, WidgetUpdateModes.FULL_WIDGET_UPDATE)

                if (PreferencesHelper.getWeatherPreference(context)) {
                    WorkManagerHelper.enqueuePeriodicWorker(
                        context,
                        WORKER_INIT_DELAY_MILLIS,
                        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
                    )
                }
            }
            WidgetActions.APPWIDGET_UPDATE -> {
                Timber.d("onReceive: Full widget update")
                WidgetController.updateAllWidgets(context, WidgetUpdateModes.FULL_WIDGET_UPDATE)
            }
            Intent.ACTION_LOCALE_CHANGED -> {
                Timber.d("onReceive: Update locale")
                WidgetController.updateAllWidgets(context, WidgetUpdateModes.LOCALE_UPDATE)
            }
            WidgetActions.WEATHER_UPDATE -> {
                Timber.d("onReceive: Update weather")
                WidgetController.updateAllWidgets(context, WidgetUpdateModes.WEATHER_UPDATE)
            }
        }
    }
}
