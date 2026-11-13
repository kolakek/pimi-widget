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
import android.content.Context
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.ui.NA
import java.util.Date

object AlarmUpdater {

    fun updateViews(
        context: Context,
        views: RemoteViews,
        settings: WidgetSettings
    ) {
        views.setTextViewText(R.id.widget_alarm_text, null)
        views.setViewVisibility(R.id.widget_alarm_icon, View.GONE)

        if (!settings.showAlarms) return

        if (settings.showPermanentAlarm) {
            views.setTextViewText(R.id.widget_alarm_text, NA)
            views.setViewVisibility(R.id.widget_alarm_icon, View.VISIBLE)
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarmMillis = alarmManager.nextAlarmClock?.triggerTime ?: return

        if (nextAlarmMillis - System.currentTimeMillis() > ALARM_LOOK_AHEAD_MILLIS) return

        views.setTextViewText(
            R.id.widget_alarm_text,
            DateFormat.getTimeFormat(context).format(Date(nextAlarmMillis))
        )
        views.setViewVisibility(R.id.widget_alarm_icon, View.VISIBLE)
    }
}
