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
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.databinding.ActivityWeatherBinding
import com.kolakek.pimiwidget.resources.WeatherString
import com.kolakek.pimiwidget.settings.AppPreferences
import com.kolakek.pimiwidget.settings.IconColor
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.weather.WeatherData
import com.kolakek.pimiwidget.weather.WeatherRenderer
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

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
        val prefs = PreferencesHelper.getAppPreferences(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.weatherData.collect { weather ->
                    weather?.let {
                        displayInfo(it, prefs)
                        displayCurrentWeather(it, prefs)
                        displayHourlyWeather(it, prefs)
                        displayDailyWeather(it, prefs)
                    }
                }
            }
        }
    }

    private fun displayInfo(weather: WeatherData, prefs: AppPreferences) {
        if (prefs.showDataTime) {
            val updateTimeStr = Instant.ofEpochMilli(weather.timeMillis)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))
            binding.dataRefreshTime.text = updateTimeStr
            binding.dataRefreshTime.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_data_refresh,
                0,
                0,
                0
            )
        }
    }

    private fun displayCurrentWeather(weather: WeatherData, prefs: AppPreferences) {
        val isDay = weather.currentIsDay() ?: return
        val weatherCode = weather.currentWeatherCode() ?: return

        val weatherStr = getString(
            WeatherString.shortStringId(weatherCode, isDay)
        )
        val currentWeather = WeatherRenderer.currentWeather(
            this,
            weather,
            prefs.tempUnit,
            prefs.iconStyle,
            IconColor.THEMED,
            false
        ) ?: return
        val feelsLikeStr = WeatherRenderer.currentFeelsLikeStr(
            this,
            weather,
            prefs.tempUnit,
        )
        val highLowStr = WeatherRenderer.dailyHighLowTempStr(
            this,
            weather,
            prefs.tempUnit,
        )
        binding.currentWeather.currentTemp.text = currentWeather.text
        binding.currentWeather.curentIcon.setImageResource(currentWeather.iconId)
        binding.currentWeather.currentString.text = weatherStr
        binding.currentWeather.currentFeelsLike.text = feelsLikeStr
        binding.currentWeather.dailyHighLow.text = highLowStr
    }

    private fun displayHourlyWeather(weather: WeatherData, prefs: AppPreferences) {
        val items = WeatherRenderer.hourlyWeather(
            this,
            weather,
            prefs.tempUnit,
            prefs.iconStyle,
            IconColor.THEMED,
        )
        binding.hourlyForecast.adapter = HourlyForecastAdapter(items)
    }

    private fun displayDailyWeather(weather: WeatherData, prefs: AppPreferences) {
        val items = WeatherRenderer.dailyWeather(
            this,
            weather,
            prefs.tempUnit,
            prefs.iconStyle,
            IconColor.THEMED,
        )
        binding.dailyForecast.adapter = DailyForecastAdapter(items)
    }
}
