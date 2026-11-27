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
import android.graphics.Paint
import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kolakek.pimiwidget.databinding.ItemAppChoiceBinding
import com.kolakek.pimiwidget.databinding.SheetAppPickerBinding
import com.kolakek.pimiwidget.settings.openUrl

fun Activity.showAppPickerSheet(
    @StringRes title: Int,
    options: List<AppOption>,
    selected: String?,
    @StringRes footerLink: Int? = null,
    footerUrl: String? = null,
    onPicked: (String) -> Unit
) {
    val dialog = BottomSheetDialog(this)
    val sheet = SheetAppPickerBinding.inflate(layoutInflater)
    sheet.title.setText(title)
    if (footerLink != null && footerUrl != null) {
        sheet.footerLink.setText(footerLink)
        sheet.footerLink.paintFlags = sheet.footerLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        sheet.footerLink.setOnClickListener { openUrl(footerUrl) }
        sheet.footerLink.visibility = View.VISIBLE
    }
    options.forEachIndexed { index, choice ->
        val row = ItemAppChoiceBinding.inflate(layoutInflater, sheet.appList, true)
        row.root.setBackgroundResource(cardBackground(index, options.size))
        row.appIcon.setImageDrawable(choice.icon)
        row.appLabel.text = choice.label
        row.iconChecked.visibility = if (choice.key == selected) View.VISIBLE else View.INVISIBLE
        row.root.setOnClickListener {
            dialog.dismiss()
            onPicked(choice.key)
        }
    }
    dialog.setContentView(sheet.root)
    dialog.show()
}
