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

package com.kolakek.pimiwidget.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withClip
import com.kolakek.pimiwidget.R
import com.kolakek.pimiwidget.weather.SunItem

class SunTrackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var data: SunItem = SunItem()
        set(value) {
            field = value
            invalidate()
        }

    private val path = Path()

    private val strokePaint = Paint().apply {
        color = "#80a5f2".toColorInt()
        style = Paint.Style.STROKE
        strokeWidth = 1f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val fillPaintAbove = Paint().apply {
        color = "#aec6f6".toColorInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val fillPaintBelow = Paint().apply {
        color = "#0b1d46".toColorInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val sunDrawable by lazy {
        AppCompatResources.getDrawable(context, R.drawable.ms) as VectorDrawable
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val sunX = data.sunX * ARC_WIDTH + ARC_LEFT
        val sunY = ARC_AMPLITUDE * data.sunY + ARC_TOP

        var horizonY = ARC_TOP + (data.horizonY * ARC_AMPLITUDE)

        horizonY = when {
            horizonY <= ARC_TOP -> ARC_TOP - ARC_SNAP
            horizonY < ARC_TOP + ARC_SNAP -> ARC_TOP + ARC_SNAP
            horizonY >= ARC_BOTTOM -> ARC_BOTTOM + ARC_SNAP
            horizonY > ARC_BOTTOM - ARC_SNAP -> ARC_BOTTOM - ARC_SNAP

            else -> horizonY
        }
        val scaleX = width / VIEWPORT_WIDTH
        val scaleY = height / VIEWPORT_HEIGHT

        canvas.scale(scaleX, scaleY)

        path.reset()
        path.moveTo(12f, 74f)
        path.cubicTo(62f, 74f, 62f, 14f, 112f, 14f)
        path.cubicTo(162f, 14f, 162f, 74f, 212f, 74f)
        path.lineTo(212f, horizonY)
        path.lineTo(12f, horizonY)
        path.close()

        canvas.withClip(0f, 0f, sunX, horizonY) {
            drawPath(path, fillPaintAbove)
        }
        canvas.drawPath(path, strokePaint)

        canvas.withClip(0f, horizonY, sunX, VIEWPORT_HEIGHT) {
            drawPath(path, fillPaintBelow)
        }
        val sunBoundsX = (sunX - SUN_SIZE / 2f).toInt()
        val sunBoundsY = (sunY - SUN_SIZE / 2f).toInt()
        sunDrawable.setBounds(sunBoundsX, sunBoundsY, sunBoundsX + SUN_SIZE, sunBoundsY + SUN_SIZE)
        sunDrawable.draw(canvas)

    }

    companion object {
        private const val SUN_SIZE = 18
        private const val VIEWPORT_WIDTH = 224f
        private const val VIEWPORT_HEIGHT = 88f
        private const val ARC_TOP = 14f
        private const val ARC_LEFT = 12f
        private const val ARC_WIDTH = 200f
        private const val ARC_AMPLITUDE = 30f
        private const val ARC_SNAP = 12f
        private const val ARC_BOTTOM = ARC_TOP + 2f * ARC_AMPLITUDE
    }
}
