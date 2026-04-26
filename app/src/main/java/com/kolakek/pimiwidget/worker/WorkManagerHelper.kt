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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.collections.firstOrNull

object WorkManagerHelper {

    fun enqueueOneTimeWorker(context: Context) {
        Timber.d("enqueueOneTimeWorker: Enqueue worker")

        val request = OneTimeWorkRequestBuilder<PimiWorker>()
            .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    fun cancelPeriodicWorker(context: Context) {
        Timber.d("cancelPeriodicWorker: Cancel worker")

        WorkManager
            .getInstance(context)
            .cancelUniqueWork(WORK_NAME)
    }

    fun enqueuePeriodicWorker(
        context: Context,
        initialDelayMillis: Long,
        existingWorkPolicy: ExistingPeriodicWorkPolicy
    ) {
        Timber.d("enqueuePeriodicWorker: Enqueue worker")

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
    }

    fun getNextScheduleMillis(context: Context): Long? {
        val workInfos = WorkManager
            .getInstance(context)
            .getWorkInfosForUniqueWork(WORK_NAME)
            .get()

        return workInfos.firstOrNull { it.state == WorkInfo.State.ENQUEUED }
            ?.nextScheduleTimeMillis
    }

    fun getWorkerStatus(context: Context): String? {
        val workInfos = WorkManager
            .getInstance(context)
            .getWorkInfosForUniqueWork(WORK_NAME)
            .get()

        return workInfos.firstOrNull()?.state?.name
    }
}
