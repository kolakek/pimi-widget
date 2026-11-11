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

import android.app.WallpaperManager
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.databinding.PrefWidgetPreviewBinding
import com.kolakek.pimiwidget.databinding.WidgetClassicBinding
import com.kolakek.pimiwidget.databinding.WidgetClassicShadowBinding
import com.kolakek.pimiwidget.databinding.WidgetModernBinding
import com.kolakek.pimiwidget.databinding.WidgetModernShadowBinding
import com.kolakek.pimiwidget.databinding.WidgetSolidBinding
import com.kolakek.pimiwidget.resources.WeatherIcon
import com.kolakek.pimiwidget.weather.WeatherCode

class WidgetPreviewPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    init {
        layoutResource = R.layout.pref_widget_preview
        isSelectable = false
    }

    fun refresh() = notifyChanged()

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val binding = PrefWidgetPreviewBinding.bind(holder.itemView)
        val inflater = LayoutInflater.from(context)
        val container = binding.previewContainer

        val wallpaperColor = WallpaperManager
            .getInstance(context)
            .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.primaryColor?.toArgb()
        wallpaperColor?.let { container.setBackgroundColor(it) }

        container.removeAllViews()

        val prefs = PreferencesHelper.getWidgetPreferences(context)

        val widget = when (prefs.widgetStyle) {
            WidgetStyle.CLASSIC ->
                WidgetClassicBinding.inflate(inflater, container, false).apply {
                    fillPreview(weatherIcon, weatherTemp, weatherAux, null, prefs, useSep = true)
                }.root

            WidgetStyle.CLASSIC_SHADOW ->
                WidgetClassicShadowBinding.inflate(inflater, container, false).apply {
                    fillPreview(weatherIcon, weatherTemp, weatherAux, null, prefs, useSep = true)
                }.root

            WidgetStyle.MODERN ->
                WidgetModernBinding.inflate(inflater, container, false).apply {
                    fillPreview(weatherIcon, weatherTemp, weatherAux, null, prefs)
                }.root

            WidgetStyle.MODERN_SHADOW ->
                WidgetModernShadowBinding.inflate(inflater, container, false).apply {
                    fillPreview(weatherIcon, weatherTemp, weatherAux, null, prefs)
                }.root

            WidgetStyle.SOLID ->
                WidgetSolidBinding.inflate(inflater, container, false).apply {
                    fillPreview(weatherIcon, weatherTemp, weatherAux, widgetBackground, prefs)
                }.root
        }
        container.addView(widget)
    }

    private fun fillPreview(
        weatherIcon: ImageView?,
        weatherTemp: TextView?,
        weatherAux: TextView?,
        background: ImageView?,
        prefs: WidgetPreferences,
        useSep: Boolean = false
    ) {
        weatherIcon?.setImageResource(
            WeatherIcon.getWeatherIconId(
                WeatherCode.MAINLY_CLEAR,
                true,
                prefs.iconStyle,
                prefs.iconColor
            )
        )
        background?.imageAlpha = prefs.backgroundAlpha
        weatherTemp?.text = context.getString(R.string.widget_temp_prev)
        val auxStr = context.getString(R.string.widget_weather_prev)
        weatherAux?.text = if (useSep) " · $auxStr" else auxStr
    }
}
