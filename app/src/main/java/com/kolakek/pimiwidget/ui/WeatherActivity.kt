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
import com.kolakek.pimiwidget.databinding.WeatherConditionBinding
import com.kolakek.pimiwidget.settings.AppPreferences
import com.kolakek.pimiwidget.settings.IconColor
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.weather.WeatherData
import com.kolakek.pimiwidget.weather.WeatherItem
import com.kolakek.pimiwidget.weather.WeatherRenderer
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding

    private val viewModel: WeatherViewModel by viewModels()

    private val hourlyAdapter = HourlyForecastAdapter()
    private val dailyAdapter = DailyForecastAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.hourlyForecast.adapter = hourlyAdapter
        binding.dailyForecast.adapter = dailyAdapter

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.content.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.weatherData.collect { weather ->
                    weather?.let {
                        val prefs = PreferencesHelper.getAppPreferences(this@WeatherActivity)

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
        } else {
            binding.dataRefreshTime.text = ""
            binding.dataRefreshTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
        binding.placeName.text = weather.place
    }

    private fun displayCurrentWeather(weather: WeatherData, prefs: AppPreferences) {
        val conditionStr = WeatherRenderer.currentConditionString(
            this,
            weather
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
        binding.currentWeather.currentIcon.setImageResource(currentWeather.iconId)
        binding.currentWeather.currentString.text = conditionStr
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
        hourlyAdapter.submitList(items)
    }

    private fun displayDailyWeather(weather: WeatherData, prefs: AppPreferences) {
        val items = WeatherRenderer.dailyWeather(
            this,
            weather,
            prefs.tempUnit,
            prefs.iconStyle,
            IconColor.THEMED,
        )
        dailyAdapter.submitList(items)
    }

    private fun displayCurrentConditions(weather: WeatherData, prefs: AppPreferences) {
        bindConditionItem(
            binding.currentWind,
            getString(R.string.app_text_title_wind),
            getString(R.string.north),
            "",
            WeatherRenderer.currentWind(this, weather),
            rotateByValue = true
        )
        bindConditionItem(
            binding.currentHumidity,
            getString(R.string.app_text_humidity),
            getString(R.string.hundred),
            getString(R.string.zero),
            WeatherRenderer.currentHumidity(this, weather, prefs.tempUnit)
        )
        bindConditionItem(
            binding.currentUv,
            getString(R.string.app_text_uv_index),
            getString(R.string.eleven_plus),
            getString(R.string.zero),
            WeatherRenderer.currentUvIndex(this, weather)
        )
        bindConditionItem(
            binding.currentPressure,
            getString(R.string.app_text_pressure),
            getString(R.string.app_text_high),
            getString(R.string.app_text_low),
            WeatherRenderer.currentPressure(this, weather),
            useUnitAsDescr = true
        )
    }

    private fun bindConditionItem(
        binding: WeatherConditionBinding,
        title: String,
        topLabel: String,
        bottomLabel: String?,
        item: WeatherItem?,
        rotateByValue: Boolean = false,
        useUnitAsDescr: Boolean = false
    ) {
        binding.textTitle.text = title
        binding.textImageTop.text = topLabel
        bottomLabel?.let { binding.textImageBottom.text = it }
        item?.let {
            binding.textValue.text = it.valueStr
            if (useUnitAsDescr) {
                binding.textUnit.text = ""
                binding.textValueDescr.text = it.unitStr
            } else {
                binding.textUnit.text = it.unitStr
                binding.textValueDescr.text = it.auxStr
            }
            binding.image.setImageResource(it.iconId)
            if (rotateByValue) binding.image.rotation = it.level.toFloat()
        }
    }
}
