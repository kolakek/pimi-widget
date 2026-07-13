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
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R

object VisibilityUpdater {

    fun updateAuxViews(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int
    ) {
        val viewHeight = context.resources.getDimensionPixelSize(R.dimen.widget_date_height) +
                context.resources.getDimensionPixelSize(R.dimen.widget_weather_height) +
                2 * context.resources.getDimensionPixelSize(R.dimen.widget_aux_height)

        val widgetHeight = AppWidgetManager
            .getInstance(context)
            .getAppWidgetOptions(appWidgetId)
            .getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            .dpToPx(context)

        if (viewHeight > widgetHeight) views.setViewVisibility(R.id.widget_aux, View.INVISIBLE)
        else views.setViewVisibility(R.id.widget_aux, View.VISIBLE)
    }

    fun updateEventViews(
        views: RemoteViews,
        birthdayUpdateStatus: BirthdayUpdateStatus
    ) {
        when (birthdayUpdateStatus) {
            BirthdayUpdateStatus.NO_BIRTHDAYS -> {
                views.setViewVisibility(R.id.widget_birthday, View.GONE)
                views.setViewVisibility(R.id.widget_alarm, View.VISIBLE)
            }
            BirthdayUpdateStatus.HAS_BIRTHDAYS -> {
                views.setViewVisibility(R.id.widget_birthday, View.VISIBLE)
                views.setViewVisibility(R.id.widget_alarm, View.GONE)
            }
        }
    }

    private fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}
