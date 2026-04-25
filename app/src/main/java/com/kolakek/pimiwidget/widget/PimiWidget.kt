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
import androidx.datastore.preferences.core.edit
import androidx.work.ExistingPeriodicWorkPolicy
import com.kolakek.pimiwidget.weather.dataStore
import com.kolakek.pimiwidget.worker.DataUpdater
import kotlinx.coroutines.runBlocking
import timber.log.Timber

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
        Timber.d("onDisabled(): Begin function.")
        DataUpdater.cancelPeriodicWorker(context)
        setWeatherPreference(context, false)
        runBlocking {
            context.dataStore.edit { it.clear() }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "android.intent.action.BOOT_COMPLETED" ||
            intent.action == "android.intent.action.MY_PACKAGE_REPLACED"
        ) {
            Timber.d("onReceive(): Update APP_WIDGET.")
            updateAppWidgetLoop(context, WidgetUpdateMode.APP_WIDGET)

            if (getWeatherPreference(context)) {
                DataUpdater.enqueuePeriodicWorker(
                    context,
                    WORKER_INIT_DELAY_MILLIS,
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
                )
            }
        } else if (intent.action == "android.intent.action.LOCALE_CHANGED") {
            Timber.d("onReceive(): Update LOCALE.")
            updateAppWidgetLoop(context, WidgetUpdateMode.LOCALE)

        } else if (intent.action == "com.kolakek.pimiwidget.action.WEATHER_UPDATE") {
            Timber.d("onReceive(): Update WEATHER_UPDATE.")
            updateAppWidgetLoop(context, WidgetUpdateMode.WEATHER)

        } else if (intent.action == "com.kolakek.pimiwidget.action.APPWIDGET_UPDATE" ||
            intent.action == "android.intent.action.WALLPAPER_CHANGED"
        ) {
            Timber.d("onReceive(): Update APP_WIDGET.")
            updateAppWidgetLoop(context, WidgetUpdateMode.APP_WIDGET)
        }
    }
}
