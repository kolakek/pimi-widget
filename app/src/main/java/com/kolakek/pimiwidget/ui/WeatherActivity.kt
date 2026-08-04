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

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kolakek.pimiwidget.databinding.ActivityWeatherBinding
import com.kolakek.pimiwidget.settings.IconColor
import com.kolakek.pimiwidget.settings.IconStyle
import com.kolakek.pimiwidget.settings.TempUnit
import com.kolakek.pimiwidget.weather.WeatherData
import com.kolakek.pimiwidget.weather.WeatherRenderer
import kotlinx.coroutines.launch

class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding

    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updatePadding(top = topInset)
            insets
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.weatherData.collect { weather ->
                    weather?.let { displayCurrentWeather(it)  }
                }
            }
        }
    }

    private fun displayCurrentWeather(weather: WeatherData) {
        val currentWeather = WeatherRenderer.getCurrentWeather(
            this,
            weather,
            IconStyle.TWINKLE_SHADOW,
            IconColor.LIGHT,
            TempUnit.CELSIUS,
            fullUnit = false
        ) ?: return

        binding.weatherCurrent.currentTemp.text = currentWeather.text
        binding.weatherCurrent.curentIcon.setImageResource(currentWeather.iconId)
    }
}
