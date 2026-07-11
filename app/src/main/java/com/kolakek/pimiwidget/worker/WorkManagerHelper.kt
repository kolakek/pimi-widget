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
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object WorkManagerHelper {

    fun cancelPeriodicWork(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
    }

    fun enqueuePeriodicWork(
        context: Context,
        initialDelayMillis: Long = 0L,
        workPolicy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP
    ) {
        val request = PeriodicWorkRequestBuilder<PimiWorker>(
            WORK_INTERVAL_MILLIS,
            TimeUnit.MILLISECONDS
        ).setInitialDelay(
            initialDelayMillis,
            TimeUnit.MILLISECONDS
        ).build()

        WorkManager
            .getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                workPolicy,
                request
            )
    }

    fun enqueueOneTimeWork(
        context: Context,
        action: UpdateAction = UpdateAction.NONE,
        workPolicy: ExistingWorkPolicy
    ) {
        val request = OneTimeWorkRequestBuilder<PimiWorker>()
            .setInputData(workDataOf(UPDATE_ACTION_KEY to action.name))
            .build()

        WorkManager
            .getInstance(context.applicationContext)
            .enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                workPolicy,
                request
            )
    }

    fun getNextRunMillis(context: Context): Long? =
        runCatching {
            WorkManager.getInstance(context.applicationContext)
                .getWorkInfosForUniqueWork(PERIODIC_WORK_NAME)
                .get()
                .firstOrNull { it.state == WorkInfo.State.ENQUEUED }
                ?.nextScheduleTimeMillis
        }.getOrNull()

    fun getStatus(context: Context): String? =
        runCatching {
            WorkManager.getInstance(context.applicationContext)
                .getWorkInfosForUniqueWork(PERIODIC_WORK_NAME)
                .get()
                .firstOrNull()
                ?.state
                ?.name
                ?.lowercase()
                ?.replaceFirstChar { it.uppercase() }
        }.getOrNull()
}
