package com.notifast.app.presentation.atoms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.notifast.app.data.model.DrawTool
import com.notifast.app.data.model.Point

@Composable
fun DrawingCanvas(
    strokes: List<com.notifast.app.data.model.Stroke>,
    onStrokeComplete: (List<Point>) -> Unit,
    tool: DrawTool,
    color: Color,
    strokeWidth: Float,
    modifier: Modifier = Modifier
) {
    var currentPoints by remember { mutableStateOf<List<Point>>(emptyList()) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(tool, color, strokeWidth) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentPoints = listOf(Point(offset.x, offset.y))
                    },
                    onDrag = { change, _ ->
                        currentPoints = currentPoints + Point(change.position.x, change.position.y)
                    },
                    onDragEnd = {
                        if (currentPoints.isNotEmpty()) {
                            onStrokeComplete(currentPoints)
                        }
                        currentPoints = emptyList()
                    },
                    onDragCancel = {
                        currentPoints = emptyList()
                    }
                )
            }
    ) {
        strokes.forEach { stroke ->
            if (stroke.points.size < 2) return@forEach
            val path = Path().apply {
                moveTo(stroke.points[0].x, stroke.points[0].y)
                for (i in 1 until stroke.points.size) {
                    lineTo(stroke.points[i].x, stroke.points[i].y)
                }
            }
            drawPath(
                path = path,
                color = Color(stroke.color),
                style = Stroke(
                    width = stroke.width,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                alpha = stroke.alpha
            )
        }

        if (currentPoints.size >= 2) {
            val path = Path().apply {
                moveTo(currentPoints[0].x, currentPoints[0].y)
                for (i in 1 until currentPoints.size) {
                    lineTo(currentPoints[i].x, currentPoints[i].y)
                }
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
                alpha = if (tool == DrawTool.HIGHLIGHTER) 0.4f else 1f
            )
        }
    }
}
