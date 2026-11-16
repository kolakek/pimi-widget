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
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.SettingsRepository
import com.kolakek.pimiwidget.data.WidgetStyle
import com.kolakek.pimiwidget.databinding.PrefIconPreviewBinding
import com.kolakek.pimiwidget.resources.WeatherIcon
import com.kolakek.pimiwidget.weather.WeatherCode

class IconPreviewPreference(context: Context, attrs: AttributeSet?) :
    Preference(context, attrs) {

    init {
        layoutResource = R.layout.pref_icon_preview
        isSelectable = false
    }

    fun refresh() = notifyChanged()

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val binding = PrefIconPreviewBinding.bind(holder.itemView)
        val container = binding.previewContainer

        val settings = SettingsRepository.loadWidgetSettings(context)

        val background = when (settings.widgetStyle) {
            WidgetStyle.CLASSIC,
            WidgetStyle.CLASSIC_SHADOW,
            WidgetStyle.MODERN,
            WidgetStyle.MODERN_SHADOW -> context.wallpaperColor()
            WidgetStyle.SOLID -> settings.widgetStyle.iconBackgroundColor(settings.widgetColor)
                ?.let { ContextCompat.getColor(context, it) }
        }
        container.setBackgroundColor(background ?: Color.TRANSPARENT)

        fun ImageView.show(code: WeatherCode, isDay: Boolean) = setImageResource(
            WeatherIcon.getWeatherIconId(code, isDay, settings.iconStyle, settings.iconColor)
        )
        with(binding) {
            icon1.show(WeatherCode.CLEAR_SKY, isDay = true)
            icon2.show(WeatherCode.MAINLY_CLEAR, isDay = false)
            icon3.show(WeatherCode.PARTLY_CLOUDY, isDay = true)
            icon4.show(WeatherCode.RAIN, isDay = true)
            icon5.show(WeatherCode.FOG, isDay = true)
            icon6.show(WeatherCode.THUNDERSTORM_AND_SKY, isDay = false)
            icon7.show(WeatherCode.OVERCAST, isDay = true)
            icon8.show(WeatherCode.HEAVY_SNOW, isDay = true)
            icon9.show(WeatherCode.THUNDERSTORM, isDay = true)
            icon10.show(WeatherCode.CLEAR_SKY, isDay = false)
            icon11.show(WeatherCode.PARTLY_CLOUDY, isDay = false)
        }
    }
}
