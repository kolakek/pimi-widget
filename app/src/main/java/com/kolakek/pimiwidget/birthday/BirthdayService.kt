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

package com.kolakek.pimiwidget.birthday

import android.content.Context
import android.provider.ContactsContract
import com.kolakek.pimiwidget.data.DataRepository
import java.time.LocalDate
import java.time.format.DateTimeParseException

object BirthdayService {

    suspend fun fetchBirthdays(
        context: Context
    ): BirthdayData {
        val today = LocalDate.now()
        val month = today.monthValue
        val day = today.dayOfMonth

        DataRepository.loadBirthdayData(context)?.let {
            if (it.month == month && it.day == day) return it
        }
        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Event.START_DATE
        )
        val selection = """
            ${ContactsContract.Data.MIMETYPE} = ? AND
            ${ContactsContract.CommonDataKinds.Event.TYPE} = ?
            """.trimIndent()

        val args = arrayOf(
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
        )
        val uri = ContactsContract.Data.CONTENT_URI

        val birthdayNames = mutableListOf<String>()

        context.contentResolver.query(uri, projection, selection, args, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow(
                ContactsContract.Contacts.DISPLAY_NAME
            )
            val dateIndex = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Event.START_DATE
            )
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                val startDate = cursor.getString(dateIndex)
                if (isBirthdayToday(startDate, month, day)) birthdayNames.add(name)
            }
        }
        val birthdayData = BirthdayData(birthdayNames, month, day)
        DataRepository.storeBirthdayData(context, birthdayData)
        return birthdayData
    }

    fun isBirthdayToday(date: String, month: Int, day: Int): Boolean {
        return try {
            if (date.startsWith("--")) {
                val parts = date.substring(2).split("-")
                parts.size == 2 && parts[0].toInt() == month && parts[1].toInt() == day
            } else {
                val localDate = LocalDate.parse(date)
                localDate.monthValue == month && localDate.dayOfMonth == day
            }
        } catch (_: DateTimeParseException) {
            false
        } catch (_: NumberFormatException) {
            false
        }
    }
}
