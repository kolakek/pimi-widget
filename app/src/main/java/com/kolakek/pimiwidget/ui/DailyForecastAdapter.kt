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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.databinding.WeatherDailyBinding
import com.kolakek.pimiwidget.weather.DailyItem

class DailyForecastAdapter :
    ListAdapter<DailyItem, DailyForecastAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: WeatherDailyBinding) : RecyclerView.ViewHolder(binding.root)

    class DiffCallback : DiffUtil.ItemCallback<DailyItem>() {
        override fun areItemsTheSame(oldItem: DailyItem, newItem: DailyItem) =
            oldItem.date == newItem.date

        override fun areContentsTheSame(oldItem: DailyItem, newItem: DailyItem) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WeatherDailyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.dailyDate.text = item.date
        holder.binding.dailyTemp.text = item.temp
        holder.binding.dailyIcon.setImageResource(item.iconId)
        when (position) {
            0 -> holder.itemView.setBackgroundResource(R.drawable.app_top_item_background)

            itemCount - 1
                -> holder.itemView.setBackgroundResource(R.drawable.app_bottom_item_background)

            else -> holder.itemView.setBackgroundResource(R.drawable.app_mid_item_background)
        }
    }
}
