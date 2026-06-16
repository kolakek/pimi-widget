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
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkManagerHelper {

    fun cancelDataWork(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(DATA_WORK_NAME)
    }

    fun enqueueDataWork(
        context: Context,
        existingWorkPolicy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP // ToDo: Remove
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<DataWorker>()
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
                existingWorkPolicy,
                request
            )
    }
}
