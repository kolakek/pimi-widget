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
import androidx.preference.ListPreference
import androidx.preference.PreferenceCategory
import com.kolakek.pimiwidget.R

class WidgetSettingsStyleFragment : CardPreferenceFragment() {
    override val titleRes = R.string.config_widget_style

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pimi_widget_prefs_style, rootKey)

        val prefs = preferenceManager.sharedPreferences ?: return

        val previewPreference = findPreference<WidgetPreviewPreference>(KEY_WIDGET_STYLE_PREV)
        val styleCategory = findPreference<PreferenceCategory>(KEY_WIDGET_STYLE_LIST)
        val widgetColorList = findPreference<ListPreference>(KEY_WIDGET_COLOR_LIST)
        val textColorList = findPreference<ListPreference>(KEY_TEXT_COLOR_LIST)
        val transparencySlider = findPreference<SliderPreference>(KEY_WIDGET_TRANSPARENCY)

        styleCategory?.addRadioGroup(
            options = WidgetStylePref.entries,
            current = WidgetStylePref.fromKey(prefs.getString(KEY_WIDGET_STYLE_LIST, null)),
            keyOf = { it.key },
            labelOf = { it.labelRes }
        ) { style ->
            prefs.edit { putString(KEY_WIDGET_STYLE_LIST, style.key) }
            matchWidgetSettings(style)
            handleVisibility(style,widgetColorList,textColorList,transparencySlider)
            previewPreference?.refresh()
        }
        textColorList?.setOptions(
            options = TextColorPref.entries,
            default = TextColorPref.DEFAULT,
            keyOf = { it.key },
            labelOf = { it.labelRes }
        ) {
            previewPreference?.refresh()
        }
        widgetColorList?.setOptions(
            options = WidgetColorPref.entries,
            default = WidgetColorPref.DEFAULT,
            keyOf = { it.key },
            labelOf = { it.labelRes }
        ) {
            previewPreference?.refresh()
        }
        transparencySlider?.onValueChange = {
            previewPreference?.refresh()
        }
        handleVisibility(
            WidgetStylePref.fromKey(prefs.getString(KEY_WIDGET_STYLE_LIST, null)),
            widgetColorList,
            textColorList,
            transparencySlider
        )
    }

    private fun matchWidgetSettings(style: WidgetStylePref) {
        val context = requireContext()
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

    private fun handleVisibility(
        widgetStylePref: WidgetStylePref,
        widgetColorList: ListPreference?,
        textColorList: ListPreference?,
        transparencySlider: SliderPreference?
    ) {
        when (widgetStylePref) {
            WidgetStylePref.SOLID -> {
                textColorList?.isVisible = false
                widgetColorList?.isVisible = true
                transparencySlider?.isVisible = true
            }

            WidgetStylePref.CLASSIC, WidgetStylePref.MODERN -> {
                textColorList?.isVisible = true
                widgetColorList?.isVisible = false
                transparencySlider?.isVisible = false
            }
        }
    }
}
