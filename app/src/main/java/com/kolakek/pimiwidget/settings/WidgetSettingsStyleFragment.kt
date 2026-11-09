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
import androidx.core.content.edit
import androidx.preference.PreferenceCategory
import com.kolakek.pimiwidget.R

class WidgetSettingsStyleFragment : CardPreferenceFragment() {
    override val titleRes = R.string.config_widget_style

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pimi_widget_prefs_style, rootKey)

        val prefs = preferenceManager.sharedPreferences ?: return
        val category: PreferenceCategory = findPreference(KEY_WIDGET_STYLE_LIST) ?: return
        val current = WidgetStylePref.fromKey(
            prefs.getString(KEY_WIDGET_STYLE_LIST, null)
        )
        val preview = findPreference<WidgetPreviewPreference>(KEY_WIDGET_STYLE_PREV)
        preview?.style = current

        val radios = WidgetStylePref.entries.associateWith { style ->
            RadioPreference(preferenceManager.context, null).apply {
                key = style.key
                setTitle(style.labelRes)
                isChecked = style == current
            }.also { category.addPreference(it) }
        }

        radios.forEach { (style, radio) ->
            radio.setOnPreferenceChangeListener { _, _ ->
                radios.forEach { (s, r) -> r.isChecked = s == style }
                prefs.edit { putString(KEY_WIDGET_STYLE_LIST, style.key) }
                onStyleSelected(style)
                preview?.style = style
                false
            }
        }
    }

    private fun onStyleSelected(style: WidgetStylePref) {
        val context = requireContext()
        PreferencesHelper.setTextColorPreference(context, PreferencesHelper.ColorPref.AUTO)
        PreferencesHelper.setIconColorPreference(context, PreferencesHelper.ColorPref.AUTO)
        PreferencesHelper.setAuxDisplayPreference(
            context,
            when (style) {
                WidgetStylePref.SOLID ->
                    PreferencesHelper.AuxDisplayPref.PLACE_CONDITION

                WidgetStylePref.CLASSIC, WidgetStylePref.MODERN ->
                    PreferencesHelper.AuxDisplayPref.NOTHING
            }
        )
    }
}
