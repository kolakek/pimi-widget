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
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.data.DataRepository
import com.kolakek.pimiwidget.data.LocationSourcePref
import com.kolakek.pimiwidget.data.SettingsRepository
import com.kolakek.pimiwidget.databinding.ActivityLocationSearchBinding
import com.kolakek.pimiwidget.databinding.ItemLocationResultBinding
import com.kolakek.pimiwidget.location.FIXED_LOCATION_NAME
import com.kolakek.pimiwidget.location.GEOCODING_MIN_QUERY_LENGTH
import com.kolakek.pimiwidget.location.GeocodingPlace
import com.kolakek.pimiwidget.location.GeocodingService
import com.kolakek.pimiwidget.location.LocationData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LocationSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationSearchBinding
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityLocationSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.resultsScroll) { view, insets ->
            val bottom = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            ).bottom
            view.updatePadding(bottom = bottom)
            insets
        }
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                startSearch()
                true
            } else false
        }
        binding.searchInput.doAfterTextChanged {
            binding.searchClear.isVisible = !it.isNullOrEmpty()
            if (it.isNullOrEmpty()) {
                searchJob?.cancel()
                binding.searchProgress.visibility = View.INVISIBLE
                binding.resultsContainer.removeAllViews()
                binding.deviceLocation.root.isVisible = true
            }
        }
        binding.searchClear.setOnClickListener {
            binding.searchInput.text.clear()
            binding.searchInput.requestFocus()
            WindowCompat.getInsetsController(
                window,
                binding.searchInput
            ).show(WindowInsetsCompat.Type.ime())
        }
        binding.searchInput.requestFocus()

        binding.deviceLocation.title.setText(R.string.config_weather_location_device)
        binding.deviceLocation.subtitle.setText(R.string.config_weather_location_device_descr)
        binding.deviceLocation.icon.setImageResource(R.drawable.ic_my_location)
        binding.deviceLocation.root.setBackgroundResource(R.drawable.app_pref_background)
        binding.deviceLocation.root.clipToOutline = true
    }

    private fun startSearch() {
        searchJob?.cancel()
        binding.resultsContainer.removeAllViews()
        val query = binding.searchInput.text.toString().trim()
        if (query.length < GEOCODING_MIN_QUERY_LENGTH) return

        WindowCompat.getInsetsController(window, binding.searchInput).hide(
            WindowInsetsCompat.Type.ime()
        )
        binding.deviceLocation.root.visibility = View.GONE
        binding.searchProgress.visibility = View.VISIBLE

        searchJob = lifecycleScope.launch {
            try {
                showResults(GeocodingService.search(query))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                binding.searchProgress.visibility = View.INVISIBLE
            }
        }
    }

    private fun showResults(places: List<GeocodingPlace>) {
        binding.searchProgress.visibility = View.INVISIBLE
        binding.resultsContainer.removeAllViews()

        places.forEachIndexed { index, place ->
            val row = ItemLocationResultBinding.inflate(
                layoutInflater,
                binding.resultsContainer,
                false
            )
            row.root.setBackgroundResource(
                cardBackground(index, places.size)
            )
            row.root.clipToOutline = true

            val parts = place.label.split(", ", limit = 2)

            row.icon.setImageResource(R.drawable.ic_location)
            row.title.text = parts[0]
            row.subtitle.text = parts.getOrNull(1).orEmpty()
            row.subtitle.isVisible = parts.size > 1
            row.root.setOnClickListener { selectFixedLocation(place) }
            binding.resultsContainer.addView(row.root)
        }
    }

    private fun cardBackground(index: Int, count: Int) = when {
        count == 1 -> R.drawable.app_pref_background
        index == 0 -> R.drawable.app_pref_background_top
        index == count - 1 -> R.drawable.app_pref_background_bottom
        else -> R.drawable.app_pref_background_mid
    }

    private fun selectFixedLocation(place: GeocodingPlace) {
        val context = applicationContext
        lifecycleScope.launch {
            DataRepository.deleteWeatherData(context)
            DataRepository.storeLocationData(
                context,
                LocationData(0, place.lat, place.long, place.name, FIXED_LOCATION_NAME)
            )
            SettingsRepository.setLocationSourcePref(context, LocationSourcePref.FIXED)
            finish()
        }
    }
}
