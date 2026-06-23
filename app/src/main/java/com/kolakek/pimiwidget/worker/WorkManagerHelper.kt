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
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import timber.log.Timber
import java.util.concurrent.TimeUnit

object WorkManagerHelper {

    fun cancelWork(context: Context) {
        Timber.d("WorkManagerHelper: cancelWork")
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(WORK_NAME)
    }

    fun enqueueWork(
        context: Context,
        workPolicy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
        isRecoveryMode: Boolean = false
    ) {
        Timber.d("WorkManagerHelper: enqueueWork isRecoveryMode = $isRecoveryMode")

        val workConfig = if (isRecoveryMode) {
            WorkConfig(NetworkType.CONNECTED, RECOVERY_INTERVAL_MILLIS, RECOVERY_DELAY_MILLIS)
        } else {
            WorkConfig(NetworkType.NOT_REQUIRED, WORK_INTERVAL_MILLIS, WORK_DELAY_MILLIS)
        }
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(workConfig.networkType)
            .build()

        val request = PeriodicWorkRequestBuilder<PimiWorker>(
            workConfig.intervalMillis,
            TimeUnit.MILLISECONDS
        )
            .setInputData(workDataOf(WORK_MODE_KEY to isRecoveryMode))
            .setInitialDelay(workConfig.initialDelayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_DELAY_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager
            .getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                workPolicy,
                request
            )
    }

    fun getNextRunMillis(context: Context): Long? =
        runCatching {
            WorkManager.getInstance(context.applicationContext)
                .getWorkInfosForUniqueWork(WORK_NAME)
                .get()
                .firstOrNull { it.state == WorkInfo.State.ENQUEUED }
                ?.nextScheduleTimeMillis
        }.getOrNull()

    fun getStatus(context: Context): String? =
        runCatching {
            WorkManager.getInstance(context.applicationContext)
                .getWorkInfosForUniqueWork(WORK_NAME)
                .get()
                .firstOrNull()
                ?.state
                ?.name
                ?.lowercase()
                ?.replaceFirstChar { it.uppercase() }
        }.getOrNull()
}
