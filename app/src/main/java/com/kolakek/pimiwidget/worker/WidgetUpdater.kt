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

package com.kolakek.pimiwidget.worker

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kolakek.pimiwidget.widget.PimiWidget
import com.kolakek.pimiwidget.data.PimiData
import com.kolakek.pimiwidget.location.LocationWorker
import com.kolakek.pimiwidget.weather.WeatherWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

class WidgetUpdater {

    companion object {

        @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION])
        internal suspend fun update(context: Context) {
            Timber.d("update(): Begin function.")

            if (permissionsDenied(context)) return

            Timber.d("update(): Update location.")

            val location = LocationWorker.getLocation(context)
            PimiData.locationState = if (location == null) STATUS_FAILED else STATUS_SUCCESS

            Timber.d("update(): Update weather.")

            val weather = WeatherWorker.getWeather(location)
            PimiData.weatherState = if (weather == null) STATUS_FAILED else STATUS_SUCCESS
            weather?.let { PimiData.weather = it }

            Timber.d("update(): Set refresh time.")

            PimiData.timeMillis = System.currentTimeMillis()

            Timber.d("update(): Update widgets.")
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, PimiWidget::class.java))

            val intent = Intent(context, PimiWidget::class.java).apply {
                action = "com.kolakek.pimiwidget.action.WEATHER_UPDATE"
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)

            Timber.d("update(): End function.")
        }

        fun enqueuePeriodicWorker(
            context: Context,
            initialDelayMillis: Long,
            existingWorkPolicy: ExistingPeriodicWorkPolicy
        ) {
            Timber.d("enqueuePeriodicWorker(): Begin function.")

            val request = PeriodicWorkRequestBuilder<PimiWorker>(
                UPDATE_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS
            ).setInitialDelay(
                initialDelayMillis,
                TimeUnit.MILLISECONDS
            ).build()

            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    existingWorkPolicy,
                    request
                )
            Timber.d("enqueuePeriodicWorker(): End function.")
        }

        fun cancelPeriodicWorker(context: Context) {
            Timber.d("cancelPeriodicWorker(): Begin function.")

            WorkManager
                .getInstance(context)
                .cancelUniqueWork(WORK_NAME)

            PimiData.reset()

            Timber.d("cancelPeriodicWorker(): End function.")
        }

        fun enqueueOneTimeWorker(context: Context) {
            Timber.d("enqueueOneTimeWorker(): Begin function.")

            val request = OneTimeWorkRequestBuilder<PimiWorker>()
                .build()

            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    ONE_TIME_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
            Timber.d("enqueueOneTimeWorker(): End function.")
        }

        fun permissionsDenied(context: Context): Boolean {
            return context.checkSelfPermission(REQUIRED_PERMISSION) == PackageManager
                .PERMISSION_DENIED
        }

        fun getWeatherStatus(): String {
            return PimiData.weatherState ?: STATUS_NAN
        }

        fun getLocationStatus(): String {
            return PimiData.locationState ?: STATUS_NAN
        }

        fun getWorkerStatus(context: Context): String {
            val workInfos = WorkManager
                .getInstance(context)
                .getWorkInfosForUniqueWork(WORK_NAME)
                .get()

            return if (workInfos.isNotEmpty()) {
                workInfos[0].state.name
            } else {
                STATUS_NAN
            }
        }

        fun getNextScheduleMillis(context: Context): Long? {
            val workInfos = WorkManager
                .getInstance(context)
                .getWorkInfosForUniqueWork(WORK_NAME)
                .get()

            return workInfos.firstOrNull { it.state == WorkInfo.State.ENQUEUED }
                ?.nextScheduleTimeMillis
        }
    }
}
