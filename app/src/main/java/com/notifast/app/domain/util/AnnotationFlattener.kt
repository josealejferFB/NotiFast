package com.notifast.app.domain.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.notifast.app.data.model.DrawTool
import com.notifast.app.data.model.PhotoAnnotation

object AnnotationFlattener {

    fun flatten(base: Bitmap, annotation: PhotoAnnotation): Bitmap {
        if (annotation.strokes.isEmpty() && annotation.texts.isEmpty() && annotation.arrows.isEmpty()) {
            return base
        }
        val output = base.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)

        annotation.strokes.forEach { stroke ->
            if (stroke.points.size < 2) return@forEach
            val paint = Paint().apply {
                color = stroke.color
                strokeWidth = stroke.width
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
                alpha = (stroke.alpha * 255).toInt().coerceIn(0, 255)
                if (stroke.tool == DrawTool.HIGHLIGHTER) {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                }
            }
            val path = Path().apply {
                moveTo(stroke.points[0].x, stroke.points[0].y)
                for (i in 1 until stroke.points.size) {
                    lineTo(stroke.points[i].x, stroke.points[i].y)
                }
            }
            canvas.drawPath(path, paint)
        }

        annotation.texts.forEach { text ->
            val paint = Paint().apply {
                color = text.color
                textSize = text.sizeSp * output.density
                isAntiAlias = true
            }
            canvas.drawText(text.text, text.x, text.y, paint)
        }

        annotation.arrows.forEach { arrow ->
            val paint = Paint().apply {
                color = arrow.color
                strokeWidth = arrow.width
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = true
            }
            canvas.drawLine(arrow.startX, arrow.startY, arrow.endX, arrow.endY, paint)
            val angle = Math.atan2(
                (arrow.endY - arrow.startY).toDouble(),
                (arrow.endX - arrow.startX).toDouble()
            )
            val headLength = arrow.width * 4
            val a1 = angle + Math.PI / 7
            val a2 = angle - Math.PI / 7
            canvas.drawLine(
                arrow.endX, arrow.endY,
                (arrow.endX - headLength * Math.cos(a1)).toFloat(),
                (arrow.endY - headLength * Math.sin(a1)).toFloat(),
                paint
            )
            canvas.drawLine(
                arrow.endX, arrow.endY,
                (arrow.endX - headLength * Math.cos(a2)).toFloat(),
                (arrow.endY - headLength * Math.sin(a2)).toFloat(),
                paint
            )
        }

        return output
    }
}
