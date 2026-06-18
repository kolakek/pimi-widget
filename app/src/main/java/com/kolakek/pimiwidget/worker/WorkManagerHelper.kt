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

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object WorkManagerHelper {

    fun cancelDataWork(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(DATA_WORK_NAME)
    }

    fun enqueueDataWork(context: Context, refreshWidget: Boolean) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<DataWorker>()
            .setInputData(workDataOf(REFRESH_WIDGET_KEY to refreshWidget))
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_DELAY_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager
            .getInstance(context.applicationContext)
            .enqueueUniqueWork(
                DATA_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
    }

    fun cancelPeriodicWidgetWork(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(PERIODIC_WIDGET_WORK_NAME)
    }

    fun enqueuePeriodicWidgetWork(context: Context) {
        val request = PeriodicWorkRequestBuilder<PimiWorker>(
            WIDGET_UPDATE_INTERVAL_MILLIS,
            TimeUnit.MILLISECONDS
        ).build()

        WorkManager
            .getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                PERIODIC_WIDGET_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    fun getNextRunMillis(context: Context, workName: String): Long? =
        runCatching {
            WorkManager.getInstance(context.applicationContext)
                .getWorkInfosForUniqueWork(workName)
                .get()
                .firstOrNull { it.state == WorkInfo.State.ENQUEUED }
                ?.nextScheduleTimeMillis
        }.getOrNull()

    fun getStatus(context: Context, workName: String): String? =
        runCatching {
            WorkManager.getInstance(context.applicationContext)
                .getWorkInfosForUniqueWork(workName)
                .get()
                .firstOrNull()
                ?.state
                ?.name
                ?.lowercase()
                ?.replaceFirstChar { it.uppercase() }
        }.getOrNull()
}
