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

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kolakek.pimiwidget.databinding.ItemAppChoiceBinding
import com.kolakek.pimiwidget.databinding.SheetAppChoiceBinding
import com.kolakek.pimiwidget.widget.WidgetIntent

fun Activity.showAppPickerSheet(
    category: String,
    @StringRes title: Int,
    selected: String?,
    onPicked: (String) -> Unit
) {
    val dialog = BottomSheetDialog(this)
    val inflater = LayoutInflater.from(this)
    val sheet = SheetAppChoiceBinding.inflate(inflater)
    sheet.title.setText(title)

    val apps = WidgetIntent.categoryApps(this, category)
    apps.forEachIndexed { index, app ->
        val row = ItemAppChoiceBinding.inflate(inflater, sheet.appList, true)
        row.root.setBackgroundResource(cardBackground(index, apps.size))
        row.appIcon.setImageDrawable(app.loadIcon(packageManager))
        row.appLabel.text = app.loadLabel(packageManager)
        row.iconChecked.visibility = if (app.packageName == selected) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
        row.root.setOnClickListener {
            onPicked(app.packageName)
            dialog.dismiss()
        }
    }

    dialog.setContentView(sheet.root)
    dialog.show()
}
