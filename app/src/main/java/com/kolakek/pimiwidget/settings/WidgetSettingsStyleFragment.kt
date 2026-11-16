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
import com.kolakek.pimiwidget.data.AuxDisplayPref
import com.kolakek.pimiwidget.data.KEY_WIDGET_TRANSPARENCY
import com.kolakek.pimiwidget.data.SettingsRepository
import com.kolakek.pimiwidget.data.TextColorPref
import com.kolakek.pimiwidget.data.WidgetColorPref
import com.kolakek.pimiwidget.data.WidgetStylePref

class WidgetSettingsStyleFragment : CardPreferenceFragment() {
    override val titleRes = R.string.config_widget_style

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pimi_widget_prefs_style, rootKey)

        val context = requireContext()

        val previewPreference: WidgetPreviewPreference? = findPreference(KEY_PREVIEW_WIDGET)
        val styleCategory: PreferenceCategory? = findPreference(KEY_CATEGORY_WIDGET_STYLE)
        val textColorCategory: PreferenceCategory? = findPreference(KEY_CATEGORY_TEXT_COLOR)
        val widgetColorCategory: PreferenceCategory? = findPreference(KEY_CATEGORY_WIDGET_COLOR)

        val transparencySlider: SliderPreference? = findPreference(KEY_WIDGET_TRANSPARENCY)

        styleCategory?.addRadioGroup(
            options = WidgetStylePref.entries,
            current = SettingsRepository.getWidgetStylePref(context),
            keyOf = { it.key },
            labelOf = { it.labelRes }
        ) { style ->
            SettingsRepository.setWidgetStylePref(context, style)
            matchWidgetSettings(style)
            handleVisibility(style, widgetColorCategory, textColorCategory, transparencySlider)
            previewPreference?.refresh()
        }
        textColorCategory?.addRadioGroup(
            options = TextColorPref.entries,
            current = SettingsRepository.getTextColorPref(context),
            keyOf = { it.key },
            labelOf = { it.labelRes }
        ) { color ->
            SettingsRepository.setTextColorPref(context, color)
            previewPreference?.refresh()
        }
        widgetColorCategory?.addRadioGroup(
            options = WidgetColorPref.entries,
            current = SettingsRepository.getWidgetColorPref(context),
            keyOf = { it.key },
            labelOf = { it.labelRes }
        ) { color ->
            SettingsRepository.setWidgetColorPref(context, color)
            previewPreference?.refresh()
        }
        transparencySlider?.onValueChange = {
            previewPreference?.refresh()
        }
        handleVisibility(
            SettingsRepository.getWidgetStylePref(context),
            widgetColorCategory,
            textColorCategory,
            transparencySlider
        )
    }

    private fun matchWidgetSettings(style: WidgetStylePref) {
        val context = requireContext()
        SettingsRepository.setAuxDisplayPref(
            context,
            when (style) {
                WidgetStylePref.SOLID ->
                    AuxDisplayPref.PLACE_CONDITION

                WidgetStylePref.CLASSIC, WidgetStylePref.MODERN ->
                    AuxDisplayPref.NOTHING
            }
        )
    }

    private fun handleVisibility(
        widgetStylePref: WidgetStylePref,
        widgetColorCategory: PreferenceCategory?,
        textColorCategory: PreferenceCategory?,
        transparencySlider: SliderPreference?
    ) {
        when (widgetStylePref) {
            WidgetStylePref.SOLID -> {
                textColorCategory?.isVisible = false
                widgetColorCategory?.isVisible = true
                transparencySlider?.isVisible = true
            }

            WidgetStylePref.CLASSIC, WidgetStylePref.MODERN -> {
                textColorCategory?.isVisible = true
                widgetColorCategory?.isVisible = false
                transparencySlider?.isVisible = false
            }
        }
    }
}
