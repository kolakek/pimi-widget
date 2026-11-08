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

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.databinding.PimiWidgetConfigureBinding
import com.kolakek.pimiwidget.utility.MigrateApp
import com.kolakek.pimiwidget.widget.WidgetUpdater

class WidgetConfigureActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)

        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        enableEdgeToEdge()

        val binding = PimiWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportFragmentManager.addOnBackStackChangedListener {
            updateBackButton()
        }
        updateBackButton()

        val isConfigured = PreferencesHelper.getWidgetConfiguredPreference(this, appWidgetId)
        val strId = if (isConfigured) R.string.config_button_update else R.string.config_button_add

        binding.applyButton.setText(strId)
        binding.applyButton.setOnClickListener { finishWidgetConfigureActivity() }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun finishWidgetConfigureActivity() {
        val context = this@WidgetConfigureActivity

        WidgetUpdater.updateWidgets(context)
        MigrateApp.storeCurrentVersionCode(context)

        PreferencesHelper.setWidgetConfiguredPreference(this, appWidgetId, true)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)

        finish()
    }

    private fun updateBackButton() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }
}
