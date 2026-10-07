package com.notifast.app.presentation.atoms

import com.notifast.app.data.model.DrawTool
import com.notifast.app.data.model.Point
import com.notifast.app.data.model.Stroke
import kotlin.math.abs
import kotlin.math.sqrt

data class DrawingCanvasState(
    val strokes: List<Stroke> = emptyList(),
    val undoneStrokes: List<Stroke> = emptyList()
) {
    fun addStroke(stroke: Stroke): DrawingCanvasState =
        copy(strokes = strokes + stroke, undoneStrokes = emptyList())

    fun undo(): DrawingCanvasState {
        if (strokes.isEmpty()) return this
        val last = strokes.last()
        return copy(
            strokes = strokes.dropLast(1),
            undoneStrokes = undoneStrokes + last
        )
    }

    fun redo(): DrawingCanvasState {
        if (undoneStrokes.isEmpty()) return this
        val last = undoneStrokes.last()
        return copy(
            strokes = strokes + last,
            undoneStrokes = undoneStrokes.dropLast(1)
        )
    }

    fun clear(): DrawingCanvasState = copy(strokes = emptyList())

    fun eraseAt(point: Point, radius: Float): DrawingCanvasState {
        val remaining = strokes.filterNot { stroke ->
            stroke.points.any { p ->
                val dx = p.x - point.x
                val dy = p.y - point.y
                sqrt(dx * dx + dy * dy) <= radius
            }
        }
        return copy(strokes = remaining)
    }

    fun canUndo(): Boolean = strokes.isNotEmpty()
    fun canRedo(): Boolean = undoneStrokes.isNotEmpty()
}
