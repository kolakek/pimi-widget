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
import android.widget.RemoteViews
import androidx.core.content.edit
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.birthday.BirthdayData
import com.kolakek.pimiwidget.resources.WidgetIcon
import com.kolakek.pimiwidget.settings.WidgetPreferences
import com.kolakek.pimiwidget.utility.KEY_PIMI_PREFERENCES
import com.kolakek.pimiwidget.utility.KEY_BIRTHDAY_DISMISS_HASH
import java.time.LocalDate

object BirthdayUpdater {

    private data class DisplayState(
        val dismissDay: Int,
        val nextIndex: Int
    )

    fun updateViews(
        context: Context,
        views: RemoteViews,
        prefs: WidgetPreferences,
        birthdayData: BirthdayData?
    ): BirthdayUpdateStatus {

        views.setTextViewText(R.id.widget_birthday, null)
        views.setTextViewCompoundDrawables(R.id.widget_birthday, 0, 0, 0, 0)

        if (!prefs.showBirthdays) return BirthdayUpdateStatus.NO_BIRTHDAYS

        birthdayData?.let { data ->
            if (data.names.isEmpty()) return BirthdayUpdateStatus.NO_BIRTHDAYS

            val today = LocalDate.now()
            val day = today.dayOfMonth
            val displayState = getDisplayState(context)

            val birthdayIndex = if (day == displayState.dismissDay) displayState.nextIndex else 0

            if (birthdayIndex == 9) return BirthdayUpdateStatus.NO_BIRTHDAYS

            data.names.getOrNull(birthdayIndex)?.let {
                val lengthCue = data.names.size - birthdayIndex - 1
                val str = if (lengthCue > 0) "$it +$lengthCue" else it

                views.setTextViewText(R.id.widget_birthday, str)
                views.setTextViewCompoundDrawables(
                    R.id.widget_birthday,
                    WidgetIcon.BIRTHDAY.id(prefs.textStyle),
                    0,
                    0,
                    0
                )
                return BirthdayUpdateStatus.HAS_BIRTHDAYS
            }
        }
        return BirthdayUpdateStatus.NO_BIRTHDAYS
    }

    fun updateDismissHash(context: Context) {
        val today = LocalDate.now()
        val day = today.dayOfMonth
        val displayState = getDisplayState(context)

        val dismissDay: Int
        val nextIndex: Int

        if (day == displayState.dismissDay) {
            dismissDay = displayState.dismissDay
            nextIndex = (displayState.nextIndex + 1).coerceAtMost(9)
        } else {
            dismissDay = day
            nextIndex = 1
        }
        setDisplayState(context, dismissDay, nextIndex)
    }

    fun deleteDismissHash(context: Context) {
        val prefs = context.getSharedPreferences(KEY_PIMI_PREFERENCES, Context.MODE_PRIVATE)
        prefs.edit { remove(KEY_BIRTHDAY_DISMISS_HASH) }
    }

    private fun getDisplayState(context: Context): DisplayState {
        val prefs = context.getSharedPreferences(KEY_PIMI_PREFERENCES, Context.MODE_PRIVATE)
        val dismissHash = prefs.getInt(KEY_BIRTHDAY_DISMISS_HASH, 0)
        return DisplayState(
            dismissDay = dismissHash / 10,
            nextIndex = dismissHash % 10
        )
    }

    private fun setDisplayState(context: Context, dismissDay: Int, nextIndex: Int) {
        val prefs = context.getSharedPreferences(KEY_PIMI_PREFERENCES, Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_BIRTHDAY_DISMISS_HASH, 10 * dismissDay + nextIndex) }
    }
}
