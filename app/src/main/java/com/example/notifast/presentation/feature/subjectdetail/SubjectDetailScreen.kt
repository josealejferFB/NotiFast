package com.example.notifast.presentation.feature.subjectdetail

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifast.presentation.atoms.NotiFastCircularLoading
import com.example.notifast.presentation.atoms.NotiFastTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    viewModel: SubjectDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { NotiFastTopBar(title = uiState.subjectName, onBackClick = onBackClick) }) { innerPadding ->
        if (uiState.isLoading) { NotiFastCircularLoading(); return@Scaffold }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            item { Text("Apuntes", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(8.dp)) }
            if (uiState.notes.isEmpty()) { item { Text("Sin apuntes") } }
            else { items(uiState.notes) { note -> Text(note.title); Spacer(Modifier.height(4.dp)) } }
            item { Spacer(Modifier.height(24.dp)); Text("Evaluaciones", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(8.dp)) }
            if (uiState.evaluations.isEmpty()) { item { Text("Sin evaluaciones") } }
            else { items(uiState.evaluations) { eval -> Text(eval.title); Spacer(Modifier.height(4.dp)) } }
        }
    }
}
