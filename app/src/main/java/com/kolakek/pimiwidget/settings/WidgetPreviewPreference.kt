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
import com.kolakek.pimiwidget.databinding.WidgetModernBinding
import com.kolakek.pimiwidget.databinding.WidgetSolidBinding

class WidgetPreviewPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    init {
        layoutResource = R.layout.pref_widget_preview
        isSelectable = false
    }

    var backgroundTransparency = 0
        set(value) {
            field = value
            notifyChanged()
        }

    var style: WidgetStylePref = WidgetStylePref.DEFAULT
        set(value) {
            field = value
            notifyChanged()
        }

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

        val widget = when (style) {
            WidgetStylePref.CLASSIC ->
                WidgetClassicBinding.inflate(inflater, container, false).apply {
                    fillPreview(
                        widgetWeatherIcon,
                        widgetWeatherTemp,
                        widgetWeatherAux,
                        useSep = true
                    )
                }.root

            WidgetStylePref.MODERN ->
                WidgetModernBinding.inflate(inflater, container, false).apply {
                    fillPreview(
                        widgetWeatherIcon,
                        widgetWeatherTemp,
                        widgetWeatherAux
                    )
                }.root

            WidgetStylePref.SOLID ->
                WidgetSolidBinding.inflate(inflater, container, false).apply {
                    fillPreview(
                        widgetWeatherIcon,
                        widgetWeatherTemp,
                        widgetWeatherAux,
                        widgetBackground
                    )
                }.root
        }
        container.addView(widget)
    }

    private fun fillPreview(
        icon: ImageView,
        temp: TextView,
        aux: TextView,
        background: ImageView? = null,
        useSep: Boolean = false
    ) {
        val tempStr = context.getString(R.string.widget_temp_prev)
        val auxStr = context.getString(R.string.widget_weather_prev)

        icon.setImageResource(R.drawable.wc_0d)
        temp.text = tempStr
        aux.text = if (useSep) " · $auxStr" else auxStr

        background?.let {
            it.imageAlpha = (100 - backgroundTransparency) * 255 / 100
        }
    }
}
