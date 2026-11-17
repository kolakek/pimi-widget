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
import com.kolakek.pimiwidget.data.AppIconStylePref
import com.kolakek.pimiwidget.data.KEY_APP_ICON_STYLE_LIST
import com.kolakek.pimiwidget.data.KEY_PRESSURE_UNIT_LIST
import com.kolakek.pimiwidget.data.KEY_RAIN_UNIT_LIST
import com.kolakek.pimiwidget.data.KEY_SNOW_UNIT_LIST
import com.kolakek.pimiwidget.data.KEY_TEMP_UNIT_LIST
import com.kolakek.pimiwidget.data.KEY_WIND_UNIT_LIST
import com.kolakek.pimiwidget.data.PressureUnitPref
import com.kolakek.pimiwidget.data.RainUnitPref
import com.kolakek.pimiwidget.data.SnowUnitPref
import com.kolakek.pimiwidget.data.TempUnitPref
import com.kolakek.pimiwidget.data.WindUnitPref

class AppSettingsFragment : CardPreferenceFragment() {
    override val titleRes = R.string.config_title_settings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pimi_app_prefs, rootKey)

        val iconStyleList: ListPreference? = findPreference(KEY_APP_ICON_STYLE_LIST)
        val tempUnitList: ListPreference? = findPreference(KEY_TEMP_UNIT_LIST)
        val rainUnitList: ListPreference? = findPreference(KEY_RAIN_UNIT_LIST)
        val snowUnitList: ListPreference? = findPreference(KEY_SNOW_UNIT_LIST)
        val windUnitList: ListPreference? = findPreference(KEY_WIND_UNIT_LIST)
        val pressureUnitList: ListPreference? = findPreference(KEY_PRESSURE_UNIT_LIST)

        tempUnitList?.setOptions(
            options = TempUnitPref.entries,
            default = TempUnitPref.DEFAULT,
            keyOf = { it.key },
            labelOf = { it.labelRes }
        )
        rainUnitList?.setOptions(
            options = RainUnitPref.entries,
            default = RainUnitPref.DEFAULT,
            keyOf = { it.key },
            labelOf = { it.labelRes }
        )
        snowUnitList?.setOptions(
            options = SnowUnitPref.entries,
            default = SnowUnitPref.DEFAULT,
            keyOf = { it.key },
            labelOf = { it.labelRes }
        )
        pressureUnitList?.setOptions(
            options = PressureUnitPref.entries,
            default = PressureUnitPref.DEFAULT,
            keyOf = { it.key },
            labelOf = { it.labelRes }
        )
        windUnitList?.setOptions(
            options = WindUnitPref.entries,
            default = WindUnitPref.DEFAULT,
            keyOf = { it.key },
            labelOf = { it.labelRes }
        )
        iconStyleList?.setOptions(
            options = AppIconStylePref.entries,
            default = AppIconStylePref.DEFAULT,
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
