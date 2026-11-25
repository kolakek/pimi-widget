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

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

fun Context.wallpaperColor(): Int? = WallpaperManager.getInstance(this)
    .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.primaryColor?.toArgb()

fun Context.isPermissionDenied(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED

fun Context.openUrl(url: String) = startActivitySafely(Intent(Intent.ACTION_VIEW, url.toUri()))

fun Context.startActivitySafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) { }
}
