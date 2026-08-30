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

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.ExistingWorkPolicy
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.databinding.ActivityWeatherBinding
import com.kolakek.pimiwidget.databinding.WeatherConditionBinding
import com.kolakek.pimiwidget.settings.AppPreferences
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.weather.DailyItem
import com.kolakek.pimiwidget.weather.HourlyItem
import com.kolakek.pimiwidget.weather.WeatherItem
import com.kolakek.pimiwidget.worker.UpdateAction
import com.kolakek.pimiwidget.worker.WorkManagerHelper
import kotlinx.coroutines.launch

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
                    val prefs = PreferencesHelper.getAppPreferences(this@WeatherActivity)

                    val displayData = weather?.let {
                        DisplayData.collect(this@WeatherActivity, it, prefs)
                    }
                    if (displayData == null || !displayData.isValid()) {
                        displayNoDataInfo(prefs)
                        binding.content.visibility = View.GONE
                        binding.noData.visibility = View.VISIBLE
                    } else {
                        displayPlace(displayData)
                        displayCurrentWeather(displayData)
                        displayHourlyWeather(displayData)
                        displayDailyWeather(displayData)
                        displayCurrentConditions(displayData)
                        binding.content.visibility = View.VISIBLE
                        binding.noData.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun displayNoDataInfo(prefs: AppPreferences) {
        if (prefs.showWeather) {
            binding.noDataTitle.text = getString(R.string.app_text_no_data)
            binding.noDataInfo.text = getString(R.string.app_text_check_connection)
            binding.noDataButton.text = getString(R.string.app_button_try_again)
            binding.noDataButton.setOnClickListener {
                WorkManagerHelper.enqueueOneTimeWork(
                    this,
                    UpdateAction.WEATHER_FETCH_THEN_REFRESH,
                    ExistingWorkPolicy.KEEP
                )
            }
        } else {
            binding.noDataTitle.text = getString(R.string.app_text_weather_off)
            binding.noDataInfo.text = getString(R.string.app_text_enable_in_settings)
            binding.noDataButton.text = getString(R.string.app_button_settings)
            binding.noDataButton.setOnClickListener {
                startActivity(Intent(this, AppConfigureActivity::class.java))
            }
        }
    }

    private fun displayPlace(data: DisplayData) {
        binding.placeName.text = data.place
    }

    private fun displayCurrentWeather(data: DisplayData) {
        binding.currentWeather.currentTemp.text = data.currentWeather?.text ?: NA
        binding.currentWeather.currentIcon.setImageResource(data.currentWeather?.iconId ?: 0)
        binding.currentWeather.currentString.text = data.currentCondition
        binding.currentWeather.currentFeelsLike.text = data.currentFeelsLike
        binding.currentWeather.dailyHighLow.text = data.dailyHighLowTemp
    }

    private fun displayHourlyWeather(data: DisplayData) {
        hourlyAdapter.submitList(data.hourlyWeather.ifEmpty { listOf(HourlyItem(NA, 0, NA)) })
    }

    private fun displayDailyWeather(data: DisplayData) {
        dailyAdapter.submitList(data.dailyWeather.ifEmpty { listOf(DailyItem(NA, 0, NA)) })
    }

    private fun displayCurrentConditions(data: DisplayData) {
        bindConditionItem(
            binding.currentWind,
            getString(R.string.app_text_title_wind),
            getString(R.string.north),
            null,
            data.currentWind,
            rotateByValue = true
        )
        bindConditionItem(
            binding.currentHumidity,
            getString(R.string.app_text_humidity),
            getString(R.string.hundred),
            getString(R.string.zero),
            data.currentHumidity
        )
        bindConditionItem(
            binding.currentUv,
            getString(R.string.app_text_uv_index),
            getString(R.string.eleven_plus),
            getString(R.string.zero),
            data.currentUvIndex
        )
        bindConditionItem(
            binding.currentPressure,
            getString(R.string.app_text_pressure),
            getString(R.string.app_text_high),
            getString(R.string.app_text_low),
            data.currentPressure,
            useUnitAsDescr = true
        )
    }

    private fun bindConditionItem(
        binding: WeatherConditionBinding,
        title: String,
        topLabel: String?,
        bottomLabel: String?,
        item: WeatherItem?,
        rotateByValue: Boolean = false,
        useUnitAsDescr: Boolean = false
    ) {
        binding.textTitle.text = title

        if (item == null) {
            binding.textValue.text = NA
            binding.textUnit.text = null
            binding.textValueDescr.text = null
            binding.textImageTop.text = null
            binding.textImageBottom.text = null
            binding.image.setImageDrawable(null)
            return
        }
        binding.textValue.text = item.valueStr

        if (useUnitAsDescr) {
            binding.textUnit.text = null
            binding.textValueDescr.text = item.unitStr
        } else {
            binding.textUnit.text = item.unitStr
            binding.textValueDescr.text = item.auxStr
        }
        binding.textImageTop.text = topLabel
        binding.textImageBottom.text = bottomLabel

        binding.image.setImageResource(item.iconId)
        if (rotateByValue) binding.image.rotation = item.level.toFloat()
    }
}
