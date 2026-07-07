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

import android.content.Context
import android.text.format.DateFormat
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.settings.AuxDisplay
import com.kolakek.pimiwidget.settings.WidgetPreferences
import java.util.Date

object AuxUpdater {

    fun updateViews(
        context: Context,
        views: RemoteViews,
        prefs: WidgetPreferences
    ) {
        views.setTextViewText(R.id.widget_aux, null)

        if (!prefs.showWeather) return

        val auxStr = when (prefs.auxDisplay) {

            AuxDisplay.NOTHING ->
                return

            AuxDisplay.UPDATE_TIME ->
                DateFormat.getTimeFormat(context).format(Date(System.currentTimeMillis()))
        }
        views.setTextViewText(
            R.id.widget_aux,
            context.getString(R.string.widget_updated_at) + " $auxStr"
        )
    }
}
