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
import androidx.preference.PreferenceViewHolder
import androidx.preference.TwoStatePreference
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.databinding.PreferenceMainSwitchBinding

class MainSwitchPreference(context: Context, attrs: AttributeSet?) : TwoStatePreference(context, attrs) {
    init { layoutResource = R.layout.preference_main_switch }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        PreferenceMainSwitchBinding.bind(holder.itemView).mainSwitch.isChecked = isChecked
    }
}
