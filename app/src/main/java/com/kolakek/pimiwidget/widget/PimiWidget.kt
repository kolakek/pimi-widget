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
import com.kolakek.pimiwidget.worker.WidgetUpdater

class PimiWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDisabled(context: Context) {
        WidgetUpdater.cancelPeriodicWorker(context)
        setWeatherPreference(context, false)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "android.intent.action.BOOT_COMPLETED" ||
            intent.action == "android.intent.action.MY_PACKAGE_REPLACED"
        ) {
            updateAppWidgetLoop(context, ::updateAppWidget)

            if (getWeatherPreference(context)) {
                WidgetUpdater.enqueuePeriodicWorker(
                    context,
                    WORKER_INIT_DELAY_MILLIS,
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
                )
            }
        } else if (intent.action == "android.intent.action.LOCALE_CHANGED") {
            updateAppWidgetLoop(context, ::updateAppWidgetLocale)

        } else if (intent.action == "com.kolakek.pimiwidget.action.WEATHER_UPDATE") {
            updateAppWidgetLoop(context, ::updateAppWidgetWeather)
        }
    }
}
