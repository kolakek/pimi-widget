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

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.databinding.PreferenceSliderBinding

class SliderPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    init {
        layoutResource = R.layout.preference_slider
        isSelectable = false
    }

    var value = 0
        private set

    var onValueChange: ((Int) -> Unit)? = null

    override fun onSetInitialValue(defaultValue: Any?) {
        value = getPersistedInt(0)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val slider = PreferenceSliderBinding.bind(holder.itemView).slider
        slider.clearOnChangeListeners()
        slider.value = value.toFloat()
        slider.addOnChangeListener { _, newValue, fromUser ->
            if (fromUser) {
                value = newValue.toInt()
                persistInt(value)
                onValueChange?.invoke(value)
            }
        }
    }
}
