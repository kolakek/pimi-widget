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
import androidx.preference.PreferenceCategory
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.SettingsRepository
import com.kolakek.pimiwidget.data.WidgetIconColorPref
import com.kolakek.pimiwidget.data.WidgetIconStylePref

class WidgetSettingsIconFragment : CardPreferenceFragment() {
    override val titleRes = R.string.config_icon_style

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pimi_widget_prefs_icon, rootKey)

        val context = requireContext()

        val previewPreference: IconPreviewPreference? = findPreference(KEY_PREVIEW_ICON)
        val styleCategory: PreferenceCategory? = findPreference(KEY_CATEGORY_ICON_STYLE)
        val colorCategory: PreferenceCategory? = findPreference(KEY_CATEGORY_ICON_COLOR)

        styleCategory?.addRadioGroup(
            options = WidgetIconStylePref.entries,
            current = SettingsRepository.getWidgetIconStylePref(context),
            keyOf = { it.key },
            labelOf = { it.labelRes }
        ) { style ->
            SettingsRepository.setWidgetIconStylePref(context, style)
            previewPreference?.refresh()
        }
        colorCategory?.addRadioGroup(
            options = WidgetIconColorPref.entries,
            current = SettingsRepository.getWidgetIconColorPref(context),
            keyOf = { it.key },
            labelOf = { it.labelRes }
        ) { color ->
            SettingsRepository.setWidgetIconColorPref(context, color)
            previewPreference?.refresh()
        }
    }
}
