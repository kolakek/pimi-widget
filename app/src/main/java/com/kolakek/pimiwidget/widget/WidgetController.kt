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
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import com.kolakek.pimiwidget.settings.PreferencesHelper
import timber.log.Timber

internal object WidgetController {

    internal fun updateAllWidgets(
        context: Context,
        updateMode: WidgetUpdateMode
    ) {
        val manager = AppWidgetManager.getInstance(context)
        manager.getAppWidgetIds(ComponentName(context, PimiWidget::class.java))
            .forEach { appWidgetId ->
                updateWidget(context, manager, appWidgetId, updateMode)
            }
    }

    internal fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        updateMode: WidgetUpdateMode
    ) {
        Timber.d("updateAppWidget: Begin Function, update mode $updateMode.")

        val prefs = PreferencesHelper.getWidgetPreferences(context)
        val views = RemoteViews(
            context.packageName,
            WidgetRenderer.getWidgetLayout(prefs.textStyle)
        )
        when (updateMode) {
            WidgetUpdateMode.FULL_UPDATE -> {
                WidgetRenderer.updateBaseWidget(context, views, appWidgetManager, appWidgetId)
                WidgetRenderer.updateDateFormat(context, views, appWidgetManager, appWidgetId)
                WidgetRenderer.updateWeather(context, views, appWidgetManager, appWidgetId, prefs)
            }
            WidgetUpdateMode.LOCALE_UPDATE -> {
                WidgetRenderer.updateDateFormat(context, views, appWidgetManager, appWidgetId)
                WidgetRenderer.updateWeather(context, views, appWidgetManager, appWidgetId, prefs)
            }
            WidgetUpdateMode.DATA_UPDATE -> {
                WidgetRenderer.updateWeather(context, views, appWidgetManager, appWidgetId, prefs)
            }
        }
    }
}
