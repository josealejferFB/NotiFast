package com.example.notifast.presentation.feature.subjectdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifast.presentation.atoms.NotiFastCircularLoading
import com.example.notifast.presentation.atoms.NotiFastCenterAlignedTopBar
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    viewModel: SubjectDetailViewModel = hiltViewModel(),
    onAddNoteClick: (String) -> Unit = {},
    onAddEvaluationClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(containerColor = Color.Transparent, topBar = { NotiFastCenterAlignedTopBar(title = uiState.subjectName) }) { innerPadding ->
            if (uiState.isLoading) { NotiFastCircularLoading(); return@Scaffold }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onAddNoteClick(uiState.subjectName) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Ruby)
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Apunte", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onAddEvaluationClick(uiState.subjectName) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Aqua)
                        ) {
                            Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Evaluación", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)); Text("Apuntes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                if (uiState.notes.isEmpty()) { item { Text("Sin apuntes", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                else {
                    items(uiState.notes) { note ->
                        Text(note.title, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(4.dp))
                    }
                }

                item { Spacer(Modifier.height(24.dp)); Text("Evaluaciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                if (uiState.evaluations.isEmpty()) { item { Text("Sin evaluaciones", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                else {
                    items(uiState.evaluations) { eval ->
                        Text(eval.title, style = MaterialTheme.typography.bodyLarge, color = if (eval.isPresented) Aqua else MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}