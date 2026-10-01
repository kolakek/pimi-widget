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

import com.kolakek.pimiwidget.R
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.pow
import kotlin.math.sqrt

object ConditionIcon {

    fun getSunTrackImage(
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
                    SUN_PX_START,
                    SUN_PX_SUNRISE
                )
            currentMillis < sunsetMillis ->
                sunTrackScaleFactor(
                    sunriseMillis,
                    sunsetMillis,
                    currentMillis,
                    SUN_PX_SUNRISE,
                    SUN_PX_SUNSET
                )
            else ->
                sunTrackScaleFactor(
                    sunsetMillis,
                    endOfDay(),
                    currentMillis,
                    SUN_PX_SUNSET,
                    SUN_PX_END
                )
        }
        val posX: Float
        val posY: Float
        val iconId: Int

        if (currentMillis in sunriseMillis..sunsetMillis) {
            iconId = R.drawable.ms_i
            posX = scaleFactor * SUN_WIDTH
            val d = posX - SUN_WIDTH / 2
            val a = SUN_CIRC_RADIUS_SQ - d.pow(2)
            posY = SUN_CIRC_CENTER - sqrt(a.coerceAtLeast(0f))
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
            posX = posX - SUN_ICON_HALF,
            posY = posY - SUN_ICON_HALF
        )
    }

    fun getWindIconId(): Int {
        return R.drawable.mw
    }

    fun getHumidityIconId(humidity: Double): Int {
        return when (humidity.toInt()) {
            in 0 .. 5 -> R.drawable.mh_0
            in 5 .. 15 -> R.drawable.mh_10
            in 15 .. 25 -> R.drawable.mh_20
            in 25 .. 35 -> R.drawable.mh_30
            in 35 .. 45 -> R.drawable.mh_40
            in 45 .. 55 -> R.drawable.mh_50
            in 55 .. 65 -> R.drawable.mh_60
            in 65 .. 75 -> R.drawable.mh_70
            in 75 .. 85 -> R.drawable.mh_80
            in 85 .. 95 -> R.drawable.mh_90
            else -> R.drawable.mh_100
        }
    }

    fun getUvIndexIconId(uvIndex: Double): Int {
        return when (uvIndex.toInt()) {
            0 -> R.drawable.mu_0
            1 -> R.drawable.mu_1
            2 -> R.drawable.mu_2
            3 -> R.drawable.mu_3
            4 -> R.drawable.mu_4
            5 -> R.drawable.mu_5
            6 -> R.drawable.mu_6
            7 -> R.drawable.mu_7
            8 -> R.drawable.mu_8
            9 -> R.drawable.mu_9
            10 -> R.drawable.mu_10
            else -> R.drawable.mu_11
        }
    }

    fun getPressureIconId(pressure: Double): Int {
        return when (pressure.toInt()) {
            in 0..970 -> R.drawable.mp_1
            in 970..985 -> R.drawable.mp_2
            in 985..998 -> R.drawable.mp_3
            in 998..1005 -> R.drawable.mp_4
            in 1005..1009 -> R.drawable.mp_5
            in 1009..1013 -> R.drawable.mp_6
            in 1013..1017 -> R.drawable.mp_7
            in 1017..1021 -> R.drawable.mp_8
            in 1021..1027 -> R.drawable.mp_9
            in 1027..1035 -> R.drawable.mp_10
            in 1035..1045 -> R.drawable.mp_11
            else -> R.drawable.mp_12
        }
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
        return (x / SUN_WIDTH).coerceIn(0f, 1f)
    }
}
