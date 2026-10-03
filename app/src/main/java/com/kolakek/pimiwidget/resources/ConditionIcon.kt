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

package com.kolakek.pimiwidget.resources

import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.kolakek.pimiwidget.R
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.pow
import kotlin.math.sqrt

object ConditionIcon {

    fun sunTrackImage(
        sunriseMillis: Long,
        sunsetMillis: Long,
        currentMillis: Long
    ): LayeredImage {
        val scaleFactor = when {
            currentMillis < sunriseMillis ->
                sunTrackScaleFactor(
                    startOfDay(),
                    sunriseMillis,
                    currentMillis,
                    MS_PX_START,
                    MS_PX_SUNRISE
                )
            currentMillis < sunsetMillis ->
                sunTrackScaleFactor(
                    sunriseMillis,
                    sunsetMillis,
                    currentMillis,
                    MS_PX_SUNRISE,
                    MS_PX_SUNSET
                )
            else ->
                sunTrackScaleFactor(
                    sunsetMillis,
                    endOfDay(),
                    currentMillis,
                    MS_PX_SUNSET,
                    MS_PX_END
                )
        }
        val posX: Float
        val posY: Float
        val iconId: Int

        if (currentMillis in sunriseMillis..sunsetMillis) {
            iconId = R.drawable.ms_i
            posX = scaleFactor * MS_WIDTH
            val d = posX - MS_WIDTH / 2
            val a = MS_CIRC_RADIUS_SQ - d.pow(2)
            posY = MS_CIRC_CENTER - sqrt(a.coerceAtLeast(0f))
        } else {
            iconId = 0
            posX = 0f
            posY = 0f
        }
        return LayeredImage(
            backResId = R.drawable.ms_b,
            fillResId = R.drawable.ms_f,
            maskResId = R.drawable.ms_m,
            iconResId = iconId,
            scaleFactor = scaleFactor,
            posX = posX - MS_ICON_HALF,
            posY = posY - MS_ICON_HALF
        )
    }

    fun humidityImage(humidity: Double): LayeredImage {
        val h = humidity.toFloat() / 100
        val posY = h * MH_PY_END + (1 - h) * MH_PY_START
        val scaleFactor = (1 - posY / MH_HEIGHT).coerceIn(0f, 1f)
        val fillColor = humidityFillColor(humidity)

        return LayeredImage(
            backResId = R.drawable.mh_b,
            fillResId = R.drawable.mh_f,
            maskResId = R.drawable.mh_m,
            iconResId = R.drawable.mh_i,
            fillColor = fillColor,
            scaleFactor = scaleFactor,
            height = MH_HEIGHT.toFloat(),
            posX = MH_PX_ICON.toFloat(),
            posY = posY - MH_ICON_HALF
        )
    }

    fun humidityFillColor(humidity: Double): Int {
        val level = (humidity / 100).coerceAtMost(1.0)

        val h = 28f + (1f - level.toFloat()) * (48f - 28f)
        val b = 0.92f + (1f - level.toFloat()) * (0.98f - 0.92f)

        return Color.HSVToColor(floatArrayOf(h, 0.85f, b))
    }

    fun humidityStrokeColor() = "#D57A2D".toColorInt()

    fun windImage(directionDeg: Double): LayeredImage = LayeredImage(
        rotation = directionDeg.toFloat(),
        iconResId = R.drawable.mw_i
    )

    fun uvIndexImage(uvIndex: Double): LayeredImage {
        val h = uvIndex.toFloat().coerceIn(0f, 11f) / 11
        val posY = h * MU_PY_END + (1 - h) * MU_PY_START
        val scaleFactor = (1 - posY / MU_HEIGHT).coerceIn(0f, 1f)

        val (fillColor, backColor) = when (uvIndex.toInt()) {
            in 0..2 -> "#53c0a8" to "#b9e0ce"
            in 3..5 -> "#ffcc5c" to "#f3ebb7"
            in 6..7 -> "#f58259" to "#fcc79f"
            in 8..10 -> "#f0516b" to "#f7a9b6"
            else -> "#7c67ae" to "#7c67ae"
        }
        return LayeredImage(
            backResId = R.drawable.mu_b,
            fillResId = R.drawable.mu_f,
            maskResId = R.drawable.mu_m,
            iconResId = R.drawable.mh_i,
            backColor = backColor.toColorInt(),
            fillColor = fillColor.toColorInt(),
            scaleFactor = scaleFactor,
            height = MU_HEIGHT.toFloat(),
            posX = MU_PX_ICON.toFloat(),
            posY = posY - MH_ICON_HALF
        )
    }

    fun pressureImage(pressure: Double): LayeredImage {
        val h = (pressure.toFloat().coerceIn(MP_P_MIN, MP_P_MAX) - MP_P_MIN) / (2 * MP_P_RANGE)
        val posY = h * MP_PY_END + (1 - h) * MP_PY_START
        val scaleFactor = (1 - posY / MP_HEIGHT).coerceIn(0f, 1f)

        return LayeredImage(
            backResId = R.drawable.mp_b,
            fillResId = R.drawable.mp_f,
            maskResId = R.drawable.mp_m,
            iconResId = R.drawable.mh_i,
            scaleFactor = scaleFactor,
            height = MP_HEIGHT.toFloat(),
            posX = MP_PX_ICON.toFloat(),
            posY = posY - MH_ICON_HALF
        )
    }

    private fun startOfDay() = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    private fun endOfDay() = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .plusDays(1)
        .toInstant()
        .toEpochMilli() - 1

    private fun sunTrackScaleFactor(t1: Long, t2: Long, t: Long, a: Int, b: Int): Float {
        val r = (t2 - t).toFloat() / (t2 - t1).toFloat()
        val x = r * a + (1 - r) * b
        return (x / MS_WIDTH).coerceIn(0f, 1f)
    }
}
