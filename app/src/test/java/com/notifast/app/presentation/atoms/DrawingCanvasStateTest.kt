package com.notifast.app.presentation.atoms

import com.notifast.app.data.model.DrawTool
import com.notifast.app.data.model.Point
import com.notifast.app.data.model.Stroke
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawingCanvasStateTest {

    private fun stroke(points: List<Point>, tool: DrawTool = DrawTool.PEN) = Stroke(
        points = points,
        color = -0x1000000,
        width = 5f,
        tool = tool
    )

    @Test
    fun `initial state has no strokes`() {
        val state = DrawingCanvasState()
        assertTrue(state.strokes.isEmpty())
        assertTrue(state.undoneStrokes.isEmpty())
    }

    @Test
    fun `add stroke appends to strokes`() {
        val state = DrawingCanvasState()
        val s = stroke(listOf(Point(0f, 0f), Point(1f, 1f)))
        val newState = state.addStroke(s)
        assertEquals(1, newState.strokes.size)
        assertEquals(s, newState.strokes[0])
    }

    @Test
    fun `undo removes last stroke and pushes to undone`() {
        val state = DrawingCanvasState()
            .addStroke(stroke(listOf(Point(0f, 0f))))
            .addStroke(stroke(listOf(Point(1f, 1f))))
        val undone = state.undo()
        assertEquals(1, undone.strokes.size)
        assertEquals(1, undone.undoneStrokes.size)
    }

    @Test
    fun `redo restores undone stroke`() {
        val state = DrawingCanvasState()
            .addStroke(stroke(listOf(Point(0f, 0f))))
        val undone = state.undo()
        val redone = undone.redo()
        assertEquals(1, redone.strokes.size)
        assertTrue(redone.undoneStrokes.isEmpty())
    }

    @Test
    fun `undo on empty state is no-op`() {
        val state = DrawingCanvasState()
        val undone = state.undo()
        assertTrue(undone.strokes.isEmpty())
    }

    @Test
    fun `redo on empty undone is no-op`() {
        val state = DrawingCanvasState()
        val redone = state.redo()
        assertTrue(redone.strokes.isEmpty())
    }

    @Test
    fun `add stroke clears undone stack`() {
        val state = DrawingCanvasState()
            .addStroke(stroke(listOf(Point(0f, 0f))))
            .undo()
            .addStroke(stroke(listOf(Point(2f, 2f))))
        assertTrue(state.undoneStrokes.isEmpty())
    }

    @Test
    fun `clear removes all strokes`() {
        val state = DrawingCanvasState()
            .addStroke(stroke(listOf(Point(0f, 0f))))
            .addStroke(stroke(listOf(Point(1f, 1f))))
        val cleared = state.clear()
        assertTrue(cleared.strokes.isEmpty())
    }

    @Test
    fun `eraser removes intersecting strokes`() {
        val s1 = stroke(listOf(Point(0f, 0f), Point(10f, 10f)))
        val s2 = stroke(listOf(Point(100f, 100f), Point(110f, 110f)))
        val state = DrawingCanvasState().addStroke(s1).addStroke(s2)
        val erased = state.eraseAt(Point(5f, 5f), radius = 20f)
        assertEquals(1, erased.strokes.size)
        assertEquals(s2, erased.strokes[0])
    }

    @Test
    fun `canUndo and canRedo reflect state`() {
        val state = DrawingCanvasState()
        assertTrue(!state.canUndo())
        assertTrue(!state.canRedo())
        val withStroke = state.addStroke(stroke(listOf(Point(0f, 0f))))
        assertTrue(withStroke.canUndo())
        val undone = withStroke.undo()
        assertTrue(undone.canRedo())
    }
}
