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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.databinding.DialogFixedLocationBinding
import com.kolakek.pimiwidget.databinding.ItemLocationResultBinding
import com.kolakek.pimiwidget.location.GEOCODING_MIN_QUERY_LENGTH
import com.kolakek.pimiwidget.location.GeocodingPlace
import com.kolakek.pimiwidget.location.GeocodingService
import com.kolakek.pimiwidget.location.LocationData
import com.kolakek.pimiwidget.location.LocationFormat
import com.kolakek.pimiwidget.location.MAX_LATITUDE
import com.kolakek.pimiwidget.location.MAX_LONGITUDE
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Dialog to choose the location used for the weather: search a place by name or enter
 * coordinates. [current] is the fixed location already set, if any.
 *
 * [onSelected] is called with the chosen place. [onUseDeviceLocation] is called when the
 * user asks to go back to the device location.
 */
class FixedLocationDialog(
    private val context: Context,
    private val scope: CoroutineScope,
    private val current: LocationData?,
    private val onSelected: (lat: Double, long: Double, place: String) -> Unit,
    private val onUseDeviceLocation: () -> Unit
) {

    private val binding = DialogFixedLocationBinding.inflate(LayoutInflater.from(context))
    private var searchJob: Job? = null
    private var dialog: AlertDialog? = null

    fun show() {
        current?.let {
            binding.latInput.setText(it.lat.toString())
            binding.longInput.setText(it.long.toString())
            binding.nameInput.setText(it.place)
        }

        // Going back to the device location only makes sense when a fixed location is set.
        if (current != null) {
            binding.deviceLocationRow.visibility = View.VISIBLE
            binding.deviceLocationRow.setOnClickListener {
                onUseDeviceLocation()
                dialog?.dismiss()
            }
        }

        val alertDialog = AlertDialog.Builder(context)
            .setTitle(R.string.config_fixed_location)
            .setView(binding.root)
            .setPositiveButton(R.string.config_fixed_location_button_use_coordinates, null)
            .setNegativeButton(R.string.config_alert_button_cancel, null)
            .create()
        alertDialog.setOnDismissListener { searchJob?.cancel() }
        alertDialog.show()
        dialog = alertDialog

        // The positive button is wired after show() so invalid input keeps the dialog open.
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            saveCoordinates()
        }
        binding.searchLayout.setEndIconOnClickListener { startSearch() }
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                startSearch()
                true
            } else false
        }
    }

    private fun startSearch() {
        val query = binding.searchInput.text?.toString()?.trim().orEmpty()
        if (query.length < GEOCODING_MIN_QUERY_LENGTH) {
            binding.resultsContainer.removeAllViews()
            showStatus(R.string.config_fixed_location_search_too_short)
            return
        }

        searchJob?.cancel()
        binding.resultsContainer.removeAllViews()
        binding.searchStatus.visibility = View.GONE
        binding.searchProgress.visibility = View.VISIBLE

        searchJob = scope.launch {
            try {
                showResults(GeocodingService.search(query))
            } catch (e: CancellationException) {
                // A newer search or a dismissed dialog cancelled this one: leave the UI alone.
                throw e
            } catch (e: Exception) {
                showStatus(R.string.config_fixed_location_search_error)
            }
        }
    }

    private fun showResults(places: List<GeocodingPlace>) {
        binding.searchProgress.visibility = View.GONE
        binding.resultsContainer.removeAllViews()
        if (places.isEmpty()) {
            showStatus(R.string.config_fixed_location_search_empty)
            return
        }

        binding.searchStatus.visibility = View.GONE
        val inflater = LayoutInflater.from(context)
        places.forEachIndexed { index, place ->
            val row = ItemLocationResultBinding.inflate(inflater, binding.resultsContainer, false)
            // The label is "Name, Region, Country": show the name big and the rest below it.
            val parts = place.label.split(", ", limit = 2)
            row.resultTitle.text = parts[0]
            row.resultSubtitle.text = parts.getOrNull(1).orEmpty()
            row.resultSubtitle.visibility = if (parts.size > 1) View.VISIBLE else View.GONE
            row.root.setBackgroundResource(itemBackground(index, places.size))
            row.root.setOnClickListener {
                onSelected(place.lat, place.long, place.label)
                dialog?.dismiss()
            }
            binding.resultsContainer.addView(row.root)
        }
    }

    // Grouped list look: rounded outer corners, small inner corners.
    @DrawableRes
    private fun itemBackground(index: Int, count: Int): Int = when {
        count == 1 -> R.drawable.dialog_item_single_background
        index == 0 -> R.drawable.dialog_item_top_background
        index == count - 1 -> R.drawable.dialog_item_bottom_background
        else -> R.drawable.dialog_item_mid_background
    }

    private fun showStatus(@StringRes messageId: Int) {
        binding.searchProgress.visibility = View.GONE
        binding.searchStatus.setText(messageId)
        binding.searchStatus.visibility = View.VISIBLE
    }

    private fun saveCoordinates() {
        val lat = LocationFormat.parseCoordinate(
            binding.latInput.text?.toString().orEmpty(),
            -MAX_LATITUDE,
            MAX_LATITUDE
        )
        val long = LocationFormat.parseCoordinate(
            binding.longInput.text?.toString().orEmpty(),
            -MAX_LONGITUDE,
            MAX_LONGITUDE
        )
        if (lat == null || long == null) {
            binding.coordinatesError.visibility = View.VISIBLE
            return
        }

        binding.coordinatesError.visibility = View.GONE
        val name = binding.nameInput.text?.toString()?.trim().orEmpty()
            .ifEmpty { LocationFormat.coordinatesLabel(lat, long) }
        onSelected(lat, long, name)
        dialog?.dismiss()
    }
}
