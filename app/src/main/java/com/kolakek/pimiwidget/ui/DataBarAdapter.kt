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
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.databinding.WeatherDataBarBinding
import com.kolakek.pimiwidget.weather.DataBarItem

class DataBarAdapter :
    ListAdapter<DataBarItem, DataBarAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: WeatherDataBarBinding) :
        RecyclerView.ViewHolder(binding.root)

    class DiffCallback : DiffUtil.ItemCallback<DataBarItem>() {
        override fun areItemsTheSame(oldItem: DataBarItem, newItem: DataBarItem) =
            oldItem.level == newItem.level

        override fun areContentsTheSame(oldItem: DataBarItem, newItem: DataBarItem) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WeatherDataBarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.dataTime.text = item.timeStr
        holder.binding.dataValueTop.text = item.valStr
        holder.binding.dataValueBottom.text = item.probStr

        val maxHeightPx = holder.itemView.resources.getDimensionPixelSize(
            R.dimen.app_bar_max_height
        )
        val params = holder.binding.dataBar.layoutParams

        params.height = (maxHeightPx * item.level).toInt().coerceAtLeast(4)
        holder.binding.dataBar.layoutParams = params

        val color = android.graphics.Color.HSVToColor(floatArrayOf(position * 15f, 0.8f, 0.8f))
        DrawableCompat.setTint(holder.binding.dataBar.background.mutate(), color)
    }
}
