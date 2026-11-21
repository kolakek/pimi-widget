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

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingWorkPolicy
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
import com.kolakek.pimiwidget.location.LocationService
import com.kolakek.pimiwidget.settings.GEOCODING_URL
import com.kolakek.pimiwidget.settings.LocationPermissionRequester
import com.kolakek.pimiwidget.settings.openUrl
import com.kolakek.pimiwidget.worker.UpdateAction
import com.kolakek.pimiwidget.worker.WorkManagerHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LocationSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationSearchBinding

    private var searchJob: Job? = null

    private val locationPermission = LocationPermissionRequester(this) { this }

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
            binding.searchClear.visibility = if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
            if (it.isNullOrEmpty()) {
                searchJob?.cancel()
                binding.searchProgress.visibility = View.INVISIBLE
                binding.resultsContainer.removeAllViews()
                binding.currentLocation.visibility = View.VISIBLE
                binding.locationFooter.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
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

        binding.fixedLocation.root.clipToOutline = true
        binding.fixedLocation.root.setOnClickListener { finish() }
        binding.fixedLocation.icon.setImageResource(R.drawable.ic_location)
        binding.fixedLocation.iconChecked.visibility = View.VISIBLE
        binding.fixedLocation.root.setBackgroundResource(R.drawable.app_pref_background_top)

        binding.deviceLocation.title.setText(R.string.config_weather_location_device)
        binding.deviceLocation.subtitle.setText(R.string.config_weather_location_device_descr)
        binding.deviceLocation.icon.setImageResource(R.drawable.ic_my_location)
        binding.deviceLocation.root.clipToOutline = true
        binding.deviceLocation.root.setOnClickListener { selectDeviceLocation() }

        binding.footerInfo.infoText.setText(R.string.config_location_search_footer)
        binding.footerInfo.root.updatePadding(bottom = 0)
        binding.footerLink.paintFlags = binding.footerLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.footerLink.setOnClickListener { openUrl(GEOCODING_URL) }

        showCurrentSelection()
    }

    override fun onResume() {
        super.onResume()
        showCurrentSelection()
    }

    private fun showCurrentSelection() {
        val context = applicationContext

        if (SettingsRepository.usesFixedLocation(context)) {
            binding.fixedLocation.root.visibility = View.VISIBLE
            binding.fixedLocation.title.setText(R.string.config_weather_location_fixed)
            binding.fixedLocation.subtitle.visibility = View.GONE
            binding.deviceLocation.root.setBackgroundResource(R.drawable.app_pref_background_bottom)
            binding.deviceLocation.iconChecked.visibility = View.GONE
            lifecycleScope.launch {
                DataRepository.loadLocationData(context)?.let {
                    binding.fixedLocation.title.text = it.place
                    binding.fixedLocation.subtitle.text = it.region
                    binding.fixedLocation.subtitle.visibility =
                        if (it.region.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        } else {
            binding.fixedLocation.root.visibility = View.GONE
            binding.deviceLocation.root.setBackgroundResource(R.drawable.app_pref_background)
            binding.deviceLocation.iconChecked.visibility =
                if (LocationService.isPermissionMissing(context)) View.GONE else View.VISIBLE
        }
    }

    private fun startSearch() {
        searchJob?.cancel()
        binding.resultsContainer.removeAllViews()

        binding.currentLocation.visibility = View.GONE
        binding.locationFooter.visibility = View.GONE
        binding.emptyState.visibility = View.GONE

        val query = binding.searchInput.text.toString().trim()

        if (query.length < GEOCODING_MIN_QUERY_LENGTH) return

        WindowCompat.getInsetsController(window, binding.searchInput).hide(
            WindowInsetsCompat.Type.ime()
        )
        binding.searchProgress.visibility = View.VISIBLE

        searchJob = lifecycleScope.launch {
            try {
                showResults(GeocodingService.search(query))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                binding.searchProgress.visibility = View.INVISIBLE
                binding.emptyState.visibility = View.VISIBLE
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
            row.icon.setImageResource(R.drawable.ic_location)
            row.title.text = place.name
            row.subtitle.text = place.region
            row.subtitle.visibility = if (place.region.isEmpty()) View.GONE else View.VISIBLE
            row.root.setOnClickListener { selectFixedLocation(place) }

            binding.resultsContainer.addView(row.root)
        }
        if (places.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.locationFooter.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.locationFooter.visibility = View.VISIBLE
        }
    }

    private fun cardBackground(index: Int, count: Int) = when {
        count == 1 -> R.drawable.app_pref_background
        index == 0 -> R.drawable.app_pref_background_top
        index == count - 1 -> R.drawable.app_pref_background_bottom
        else -> R.drawable.app_pref_background_mid
    }

    private fun selectDeviceLocation() {
        val context = applicationContext
        locationPermission.request {
            if (SettingsRepository.usesDeviceLocation(context)) {
                finish()
            } else {
                lifecycleScope.launch {
                    DataRepository.deleteLocationData(context)
                    applyLocationSource(context, LocationSourcePref.DEVICE)
                    finish()
                }
            }
        }
    }

    private fun selectFixedLocation(place: GeocodingPlace) {
        val context = applicationContext
        lifecycleScope.launch {
            val data = LocationData(0,
                place.lat,
                place.long,
                place.name,
                place.region,
                FIXED_LOCATION_NAME
            )
            DataRepository.storeLocationData(context, data)
            applyLocationSource(context, LocationSourcePref.FIXED)
            finish()
        }
    }

    private suspend fun applyLocationSource(context: Context, source: LocationSourcePref) {
        SettingsRepository.setLocationSourcePref(context, source)
        DataRepository.deleteWeatherData(context)

        if (!SettingsRepository.isWeatherEnabled(context)) return

        if (LocationService.isPermissionMissing(context)) {
            SettingsRepository.setWeatherEnabled(context, false)
        } else {
            WorkManagerHelper.enqueueOneTimeWork(
                context,
                UpdateAction.WEATHER_FETCH_THEN_REFRESH,
                ExistingWorkPolicy.REPLACE
            )
        }
    }
}
