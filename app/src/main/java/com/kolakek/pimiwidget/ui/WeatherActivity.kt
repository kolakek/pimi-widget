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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.content.updatePadding(top = bars.top, bottom = bars.bottom)
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
                        displayCurrentConditions(it, prefs)
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
        binding.placeName.text = weather.place
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

    private fun displayCurrentConditions(weather: WeatherData, prefs: AppPreferences) {
        val windItem = WeatherRenderer.currentWind(
            this,
            weather
        )
        binding.currentWind.textTitle.text = getString(R.string.app_text_title_wind)
        binding.currentWind.textImageTop.text = getString(R.string.north)
        windItem?.let {
            binding.currentWind.image.setImageResource(it.iconId)
            binding.currentWind.textValue.text = it.valueStr
            binding.currentWind.textUnit.text = it.unitStr
            binding.currentWind.textValueDescr.text = it.auxStr
            binding.currentWind.image.rotation = it.level.toFloat()
        }
        val humidityItem = WeatherRenderer.currentHumidity(
            this,
            weather,
            prefs.tempUnit
        )
        binding.currentHumidity.textTitle.text = getString(R.string.app_text_humidity)
        binding.currentHumidity.textImageTop.text = getString(R.string.hundred)
        binding.currentHumidity.textImageBottom.text = getString(R.string.zero)
        humidityItem?.let {
            binding.currentHumidity.textValue.text = it.valueStr
            binding.currentHumidity.textUnit.text = it.unitStr
            binding.currentHumidity.textValueDescr.text = it.auxStr
            binding.currentHumidity.image.setImageResource(it.iconId)
        }
        val uvIndexItem = WeatherRenderer.currentUvIndex(
            this,
            weather
        )
        binding.currentUv.textTitle.text = getString(R.string.app_text_uv_index)
        binding.currentUv.textImageTop.text = getString(R.string.eleven_plus)
        binding.currentUv.textImageBottom.text = getString(R.string.zero)
        uvIndexItem?.let {
            binding.currentUv.textValue.text = it.valueStr
            binding.currentUv.textUnit.text = it.unitStr
            binding.currentUv.textValueDescr.text = it.auxStr
            binding.currentUv.image.setImageResource(it.iconId)
        }
        binding.currentPressure.textTitle.text = "Pressure"
        binding.currentPressure.textValue.text = "1,013"
        binding.currentPressure.textUnit.text = ""
        binding.currentPressure.textValueDescr.text = "mBar"
        binding.currentPressure.textImageTop.text = ""
        binding.currentPressure.textImageBottom.text = "Low   High"
        binding.currentPressure.image.setImageResource(R.drawable.mp_4)
    }
}
