package com.notifast.app.presentation.feature.noteeditor.annotator

import androidx.lifecycle.ViewModel
import com.notifast.app.data.model.DrawTool
import com.notifast.app.data.model.Point
import com.notifast.app.data.model.Stroke
import com.notifast.app.presentation.atoms.DrawingCanvasState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PhotoAnnotatorViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoAnnotatorUiState())
    val uiState: StateFlow<PhotoAnnotatorUiState> = _uiState.asStateFlow()

    fun init(imagePath: String) {
        _uiState.update { it.copy(imagePath = imagePath) }
    }

    fun onEvent(event: PhotoAnnotatorEvent) {
        when (event) {
            is PhotoAnnotatorEvent.ToolSelected -> _uiState.update {
                it.copy(currentTool = event.tool)
            }
            is PhotoAnnotatorEvent.ColorSelected -> _uiState.update {
                it.copy(currentColor = event.color)
            }
            is PhotoAnnotatorEvent.WidthSelected -> _uiState.update {
                it.copy(currentWidth = event.width)
            }
            is PhotoAnnotatorEvent.StrokeAdded -> {
                val state = _uiState.value
                if (event.points.isEmpty()) return
                val stroke = Stroke(
                    points = event.points,
                    color = if (state.currentTool == DrawTool.HIGHLIGHTER) {
                        state.currentColor or (0x66 shl 24)
                    } else {
                        state.currentColor
                    },
                    width = state.currentWidth,
                    tool = state.currentTool,
                    alpha = if (state.currentTool == DrawTool.HIGHLIGHTER) 0.4f else 1f
                )
                _uiState.update { it.copy(canvasState = it.canvasState.addStroke(stroke)) }
            }
            is PhotoAnnotatorEvent.Undo -> _uiState.update {
                it.copy(canvasState = it.canvasState.undo())
            }
            is PhotoAnnotatorEvent.Redo -> _uiState.update {
                it.copy(canvasState = it.canvasState.redo())
            }
            is PhotoAnnotatorEvent.Clear -> _uiState.update {
                it.copy(canvasState = it.canvasState.clear())
            }
            is PhotoAnnotatorEvent.Save -> _uiState.update { it.copy(isSaved = true) }
            is PhotoAnnotatorEvent.Cancel -> _uiState.update { it.copy(isSaved = true) }
        }
    }
}
