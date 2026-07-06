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

package com.kolakek.pimiwidget.utility

import android.content.Context
import com.kolakek.pimiwidget.settings.PreferencesHelper
import com.kolakek.pimiwidget.widget.WidgetUpdater
import com.kolakek.pimiwidget.worker.WorkManagerHelper

internal object MigrateApp {

    fun migrate(context: Context) {
        setFirstAvailableWeatherApp(context)

        WidgetUpdater.updateWidgets(context)

        WorkManagerHelper.cancelWork(context)
        if (PreferencesHelper.getWeatherPreference(context)) {
            WorkManagerHelper.enqueueWork(context)
        }
    }

    private fun setFirstAvailableWeatherApp(context: Context) {
        WeatherApp.entries.firstOrNull { app ->
            AppLookup.isAppInstalled(context, app.packageName)
        }?.let { PreferencesHelper.setWeatherAppPreference(context, it.key) }
    }
}
