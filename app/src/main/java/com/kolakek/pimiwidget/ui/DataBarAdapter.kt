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
import androidx.recyclerview.widget.RecyclerView
import com.kolakek.pimiwidget.databinding.WeatherDataBarBinding

class DataBarAdapter : RecyclerView.Adapter<DataBarAdapter.ViewHolder>() {

    class ViewHolder(val binding: WeatherDataBarBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WeatherDataBarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.dataAmount.text = position.toString()
        holder.binding.dataTime.text = "${position}:00"
        val maxHeightPx = (48 * holder.itemView.resources.displayMetrics.density).toInt()
        val params = holder.binding.dataBar.layoutParams
        params.height = (maxHeightPx * (position / 23f)).toInt()
        val color = android.graphics.Color.HSVToColor(floatArrayOf(position * 15f, 0.8f, 0.8f))
        DrawableCompat.setTint(holder.binding.dataBar.background.mutate(), color)
        holder.binding.dataBar.layoutParams = params
    }

    override fun getItemCount() = 24
}
