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

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.ListPreference
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.KEY_TEMP_UNIT_LIST
import com.kolakek.pimiwidget.data.TempUnitPref

class AppSettingsFragment : CardPreferenceFragment() {
    override val titleRes = R.string.config_title_settings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pimi_app_prefs, rootKey)

        val tempUnitList: ListPreference? = findPreference(KEY_TEMP_UNIT_LIST)

        tempUnitList?.setOptions(
            options = TempUnitPref.entries,
            default = TempUnitPref.DEFAULT,
            keyOf = { it.key },
            labelOf = { it.labelRes }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView.clipToPadding = false

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            listView.updatePadding(bottom = bottomInset)
            insets
        }
    }
}
