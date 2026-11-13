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

package com.kolakek.pimiwidget.settings

import androidx.preference.ListPreference
import androidx.preference.PreferenceCategory

fun <T> PreferenceCategory.addRadioGroup(
    options: List<T>,
    current: T,
    keyOf: (T) -> String,
    labelOf: (T) -> Int,
    onSelected: (T) -> Unit,
) {
    val radios = options.mapIndexed { index, option ->
        option to RadioPreference(context, null).apply {
            key = keyOf(option)
            setTitle(labelOf(option))
            order = index
            isChecked = option == current
        }.also { addPreference(it) }
    }

    radios.forEach { (option, radio) ->
        radio.setOnPreferenceChangeListener { _, _ ->
            radios.forEach { (o, r) -> r.isChecked = o == option }
            onSelected(option)
            false
        }
    }
}

fun <T> ListPreference.setOptions(
    options: List<T>,
    default: T,
    keyOf: (T) -> String,
    labelOf: (T) -> Int,
    onSelected: (T) -> Unit = {},
) {
    entries = options.map { context.getString(labelOf(it)) }.toTypedArray()
    entryValues = options.map(keyOf).toTypedArray()

    if (findIndexOfValue(value) < 0) value = keyOf(default)

    setOnPreferenceChangeListener { _, newValue ->
        options.firstOrNull { keyOf(it) == newValue }?.let(onSelected)
        true
    }
}
