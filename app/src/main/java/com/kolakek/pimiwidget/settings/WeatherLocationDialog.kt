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

package com.kolakek.pimiwidget.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.databinding.DialogWeatherLocationBinding
import com.kolakek.pimiwidget.databinding.ItemLocationResultBinding
import com.kolakek.pimiwidget.location.GEOCODING_MIN_QUERY_LENGTH
import com.kolakek.pimiwidget.location.GeocodingPlace
import com.kolakek.pimiwidget.location.GeocodingService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WeatherLocationDialog(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onDeviceLocationSelected: () -> Unit,
    private val onFixedLocationSelected: (lat: Double, long: Double, place: String) -> Unit,
) {
    private val binding = DialogWeatherLocationBinding.inflate(LayoutInflater.from(context))
    private var searchJob: Job? = null
    private var dialog: AlertDialog? = null

    fun show() {
        binding.deviceLocation.setOnClickListener {
            onDeviceLocationSelected()
            dialog?.dismiss()
        }
        binding.searchLayout.setEndIconOnClickListener { startSearch() }
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                startSearch()
                true
            } else false
        }
        dialog = AlertDialog.Builder(context)
            .setTitle(R.string.config_weather_location)
            .setView(binding.root)
            .setNegativeButton(R.string.config_alert_button_cancel, null)
            .setOnDismissListener { searchJob?.cancel() }
            .show()
    }

    private fun startSearch() {
        searchJob?.cancel()
        val query = binding.searchInput.text?.toString()?.trim().orEmpty()
        if (query.length < GEOCODING_MIN_QUERY_LENGTH) {
            binding.resultsContainer.removeAllViews()
            showStatus(R.string.config_fixed_location_search_too_short)
            return
        }
        binding.resultsContainer.removeAllViews()
        binding.searchStatus.visibility = View.GONE
        binding.searchProgress.visibility = View.VISIBLE

        searchJob = scope.launch {
            try {
                showResults(GeocodingService.search(query))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                showStatus(R.string.config_fixed_location_search_error)
            }
        }
    }

    private fun showResults(places: List<GeocodingPlace>) {
        binding.searchProgress.visibility = View.GONE
        binding.resultsContainer.removeAllViews()
        binding.searchStatus.visibility = View.GONE

        if (places.isEmpty()) {
            showStatus(R.string.config_fixed_location_search_empty)
            return
        }
        val inflater = LayoutInflater.from(context)
        places.forEach { place ->
            val row = ItemLocationResultBinding.inflate(inflater, binding.resultsContainer, false)
            val parts = place.label.split(", ", limit = 2)

            row.resultTitle.text = parts[0]
            row.resultSubtitle.text = parts.getOrNull(1).orEmpty()
            row.resultSubtitle.visibility = if (parts.size > 1) View.VISIBLE else View.GONE
            row.root.setOnClickListener {
                onFixedLocationSelected(place.lat, place.long, place.name)
                dialog?.dismiss()
            }
            binding.resultsContainer.addView(row.root)
        }
    }

    private fun showStatus(@StringRes messageId: Int) {
        binding.searchProgress.visibility = View.GONE
        binding.searchStatus.setText(messageId)
        binding.searchStatus.visibility = View.VISIBLE
    }
}
