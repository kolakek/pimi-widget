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
import android.widget.RemoteViews
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.resources.WidgetIcon
import com.kolakek.pimiwidget.settings.WidgetPreferences
import java.util.Date

object AlarmUpdater {

    fun updateViews(
        context: Context,
        views: RemoteViews,
        prefs: WidgetPreferences
    ) {
        views.setTextViewText(R.id.widget_alarm, null)
        views.setTextViewCompoundDrawables(R.id.widget_alarm, 0, 0, 0, 0)

        if (!prefs.showAlarms) return

        if (prefs.permanentAlarm) showIcon(views, prefs)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarmMillis = alarmManager.nextAlarmClock?.triggerTime ?: return

        if (nextAlarmMillis - System.currentTimeMillis() > ALARM_LOOK_AHEAD_MILLIS) return

        views.setTextViewText(
            R.id.widget_alarm,
            DateFormat.getTimeFormat(context).format(Date(nextAlarmMillis))
        )
        showIcon(views, prefs)
    }

    private fun showIcon(views: RemoteViews, prefs: WidgetPreferences) {
        views.setTextViewCompoundDrawables(
            R.id.widget_alarm,
            WidgetIcon.ALARM.id(prefs.textStyle),
            0,
            0,
            0
        )
    }
}
