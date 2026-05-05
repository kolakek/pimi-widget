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

package com.kolakek.pimiwidget.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.kolakek.pimiwidget.databinding.PimiWidgetConfigureBinding

class WidgetConfigureActivity : FragmentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private fun finishWidgetConfigureActivity() {
        val context = this@WidgetConfigureActivity

        WidgetController.updateWidget(
            context,
            AppWidgetManager.getInstance(context),
            appWidgetId
        )
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val binding = PimiWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.post {
            val statusBarHeight =
                ViewCompat.getRootWindowInsets(binding.root)
                    ?.getInsets(WindowInsetsCompat.Type.statusBars())
                    ?.top ?: 0

            binding.configTitleText.setPadding(
                binding.configTitleText.paddingLeft,
                binding.configTitleText.paddingTop,
                binding.configTitleText.paddingRight,
                statusBarHeight
            )
        }
        binding.button.setOnClickListener { finishWidgetConfigureActivity() }
    }
}
