package com.notifast.app.presentation.feature.noteeditor.annotator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.notifast.app.presentation.atoms.DrawingCanvas

@Composable
fun PhotoAnnotatorScreen(
    imagePath: String,
    onSave: (com.notifast.app.data.model.PhotoAnnotation) -> Unit,
    onCancel: () -> Unit,
    viewModel: PhotoAnnotatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(imagePath) { viewModel.init(imagePath) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            val annotation = com.notifast.app.data.model.PhotoAnnotation(
                strokes = uiState.canvasState.strokes
            )
            onSave(annotation)
        }
    }

    LaunchedEffect(uiState.isCancelled) {
        if (uiState.isCancelled) onCancel()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            AnnotatorToolbar(
                currentTool = uiState.currentTool,
                currentColor = uiState.currentColor,
                currentWidth = uiState.currentWidth,
                canUndo = uiState.canvasState.canUndo(),
                canRedo = uiState.canvasState.canRedo(),
                onToolSelected = { viewModel.onEvent(PhotoAnnotatorEvent.ToolSelected(it)) },
                onColorSelected = { viewModel.onEvent(PhotoAnnotatorEvent.ColorSelected(it)) },
                onWidthSelected = { viewModel.onEvent(PhotoAnnotatorEvent.WidthSelected(it)) },
                onUndo = { viewModel.onEvent(PhotoAnnotatorEvent.Undo) },
                onRedo = { viewModel.onEvent(PhotoAnnotatorEvent.Redo) },
                onClear = { viewModel.onEvent(PhotoAnnotatorEvent.Clear) },
                onSave = { viewModel.onEvent(PhotoAnnotatorEvent.Save) },
                onCancel = { viewModel.onEvent(PhotoAnnotatorEvent.Cancel) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AsyncImage(
                model = uiState.imagePath,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            DrawingCanvas(
                strokes = uiState.canvasState.strokes,
                onStrokeComplete = { points ->
                    viewModel.onEvent(PhotoAnnotatorEvent.StrokeAdded(points))
                },
                tool = uiState.currentTool,
                color = Color(uiState.currentColor),
                strokeWidth = uiState.currentWidth,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
