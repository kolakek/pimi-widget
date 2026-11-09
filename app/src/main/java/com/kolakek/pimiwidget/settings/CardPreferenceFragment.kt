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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import androidx.preference.Preference
import androidx.preference.PreferenceGroupAdapter
import androidx.preference.PreferenceViewHolder
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.kolakek.pimiwidget.R

abstract class CardPreferenceFragment : PreferenceFragmentCompat() {

    @get:StringRes
    abstract val titleRes: Int

    @SuppressLint("RestrictedApi")
    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> =
        object : PreferenceGroupAdapter(preferenceScreen) {
            override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                val view = holder.itemView
                val item = getItem(position) ?: return

                when {
                    item is PreferenceCategory -> view.updatePaddingRelative(
                        start = view.resources.getDimensionPixelSize(
                            R.dimen.app_pref_category_padding_start
                        )
                    )
                    item.parent is PreferenceCategory -> view.setBackgroundResource(
                        cardBackground(item, position)
                    )
                }
            }

            private fun cardBackground(item: Preference, pos: Int): Int {
                val isFirst = pos == 0 || getItem(pos - 1)?.parent != item.parent
                val isLast = pos == itemCount - 1 || getItem(pos + 1)?.parent != item.parent

                return when {
                    isFirst && isLast -> R.drawable.app_pref_background
                    isFirst -> R.drawable.app_pref_background_top
                    isLast -> R.drawable.app_pref_background_bottom
                    else -> R.drawable.app_pref_background_mid
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().setTitle(titleRes)

        val horizontal = resources.getDimensionPixelSize(R.dimen.app_pref_card_margin)
        listView.updatePadding(left = horizontal, right = horizontal)

        listView.scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY

        listView.isVerticalScrollBarEnabled = false
        listView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        recyclerView.isVerticalScrollBarEnabled = true
                        recyclerView.removeOnScrollListener(this)
                    }
                }
            }
        )
        listView.clipToPadding = false
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            listView.updatePadding(bottom = bottomInset)
            insets
        }
        setDivider(null)
    }
}
