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
        updateMode: WidgetUpdateMode = WidgetUpdateMode.FULL_WIDGET_UPDATE
    ) {
        Timber.d("updateAppWidget: Begin Function, update mode $updateMode.")

        val prefs = PreferencesHelper.getWidgetPreferences(context)
        val views = WidgetRenderer.buildRemoteViews(context, prefs)

        if (updateMode == WidgetUpdateMode.FULL_WIDGET_UPDATE) {
            WidgetRenderer.updateBaseWidget(context, views, appWidgetManager, appWidgetId)
        }
        if (updateMode == WidgetUpdateMode.FULL_WIDGET_UPDATE ||
            updateMode == WidgetUpdateMode.LOCALE_UPDATE
        ) {
            WidgetRenderer.updateDate(context, views, appWidgetManager, appWidgetId)
        }
        if (updateMode == WidgetUpdateMode.FULL_WIDGET_UPDATE ||
            updateMode == WidgetUpdateMode.LOCALE_UPDATE ||
            updateMode == WidgetUpdateMode.WEATHER_UPDATE
        ) {
            WidgetRenderer.updateWeather(context, views, appWidgetManager, appWidgetId, prefs)
        }
    }
}
