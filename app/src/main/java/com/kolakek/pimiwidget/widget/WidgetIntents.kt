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

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent

internal object WidgetIntents {

    internal fun categoryIntent(
        context: Context,
        category: String,
        requestCode: Int
    ): PendingIntent? {

        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)

        if (intent.resolveActivity(context.packageManager) == null) {
            val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)
            resolveInfos.firstOrNull()?.activityInfo?.let {
                intent.setComponent(ComponentName(it.packageName, it.name))
            }
        }

        return intent.resolveActivity(context.packageManager)?.let {
            PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    internal fun altWeatherAppIntent(
        context: Context,
        requestCode: Int
    ): PendingIntent? {

        return context.packageManager.getLaunchIntentForPackage(ALT_WEATHER_APP)?.let {
            PendingIntent.getActivity(
                context,
                requestCode,
                it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}
