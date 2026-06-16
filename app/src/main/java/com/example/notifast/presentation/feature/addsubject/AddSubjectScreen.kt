package com.example.notifast.presentation.feature.addsubject

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifast.presentation.atoms.NotiFastButton
import com.example.notifast.presentation.atoms.NotiFastCenterAlignedTopBar
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.atoms.NotiFastTextField
import com.example.notifast.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectScreen(
    viewModel: AddSubjectViewModel = hiltViewModel(),
    onSubjectCreated: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isSaved) {
        onSubjectCreated(uiState.subjectName)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { NotiFastCenterAlignedTopBar(title = "Nueva Materia") }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                NotiFastTextField(
                    value = uiState.subjectName,
                    onValueChange = { viewModel.onEvent(AddSubjectEvent.NameChanged(it)) },
                    label = "Nombre de la materia",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))
                Text("Icono", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                val emojis = listOf("📚", "🔬", "📐", "🎨", "⚽", "🌍", "📝", "🧮", "💻", "🧪", "📊", "🎵", "🎭", "🏛️", "🌿", "⚡", "💎", "🎯", "🧩", "🎪")
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(emojis) { emoji ->
                        val selected = uiState.selectedIcon == emoji
                        Surface(
                            onClick = { viewModel.onEvent(AddSubjectEvent.IconSelected(emoji)) },
                            shape = CircleShape,
                            color = if (selected) Ruby else Color.White.copy(alpha = 0.9f),
                            border = if (selected) null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Text(emoji, modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                uiState.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }
                NotiFastButton(
                    text = "CREAR MATERIA",
                    onClick = { viewModel.onEvent(AddSubjectEvent.SaveClicked) },
                    isLoading = uiState.isLoading
                )
            }
        }
    }
}
