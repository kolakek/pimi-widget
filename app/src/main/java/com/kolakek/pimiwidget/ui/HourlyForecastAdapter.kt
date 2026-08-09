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

package com.kolakek.pimiwidget.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kolakek.pimiwidget.databinding.WeatherHourlyBinding
import com.kolakek.pimiwidget.weather.HourlyItem

class HourlyForecastAdapter(
    private val items: List<HourlyItem>
) : RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>() {

    class ViewHolder(val binding: WeatherHourlyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WeatherHourlyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.hourlyTime.text = item.time
        holder.binding.hourlyTemp.text = item.temp
        holder.binding.hourlyIcon.setImageResource(item.iconId)
    }

    override fun getItemCount() = items.size
}
