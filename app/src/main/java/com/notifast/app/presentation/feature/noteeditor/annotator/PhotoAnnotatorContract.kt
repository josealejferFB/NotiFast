package com.notifast.app.presentation.feature.noteeditor.annotator

import com.notifast.app.data.model.DrawTool
import com.notifast.app.data.model.PhotoAnnotation
import com.notifast.app.presentation.atoms.DrawingCanvasState

data class PhotoAnnotatorUiState(
    val imagePath: String = "",
    val canvasState: DrawingCanvasState = DrawingCanvasState(),
    val currentTool: DrawTool = DrawTool.PEN,
    val currentColor: Int = android.graphics.Color.BLACK,
    val currentWidth: Float = 5f,
    val isSaved: Boolean = false,
    val isCancelled: Boolean = false,
    val error: String? = null
)

sealed interface PhotoAnnotatorEvent {
    data class ToolSelected(val tool: DrawTool) : PhotoAnnotatorEvent
    data class ColorSelected(val color: Int) : PhotoAnnotatorEvent
    data class WidthSelected(val width: Float) : PhotoAnnotatorEvent
    data class StrokeAdded(val points: List<com.notifast.app.data.model.Point>) : PhotoAnnotatorEvent
    data object Undo : PhotoAnnotatorEvent
    data object Redo : PhotoAnnotatorEvent
    data object Clear : PhotoAnnotatorEvent
    data object Save : PhotoAnnotatorEvent
    data object Cancel : PhotoAnnotatorEvent
}
