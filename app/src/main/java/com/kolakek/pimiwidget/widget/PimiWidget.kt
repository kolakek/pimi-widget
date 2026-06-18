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
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.worker.WorkManagerHelper
import timber.log.Timber

class PimiWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        WidgetUpdater.updateWidgets(context, canEnqueueDataWork = true)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WorkManagerHelper.enqueuePeriodicWidgetWork(context)
    }

    override fun onDisabled(context: Context) {
        PreferencesHelper.setWeatherPreference(context, false)
        WorkManagerHelper.cancelDataWork(context)
        WorkManagerHelper.cancelPeriodicWidgetWork(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Timber.d("onReceive: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_LOCALE_CHANGED, PIMI_ACTION_WIDGET_UPDATE ->
                WidgetUpdater.updateWidgets(context, canEnqueueDataWork = false)
        }
    }
}
