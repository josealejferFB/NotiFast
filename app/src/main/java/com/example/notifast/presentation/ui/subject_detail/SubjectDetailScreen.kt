package com.example.notifast.presentation.ui.subject_detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notifast.data.model.Note
import com.example.notifast.presentation.common.viewmodel.NotesViewModel
import com.example.notifast.presentation.ui.notes.components.NoteDetailExpandable
import com.example.notifast.presentation.ui.notes.components.NoteEstheticCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectName: String,
    viewModel: NotesViewModel,
    onEditNote: (Note) -> Unit,
    onBack: () -> Unit
) {
    val subjectsMap by viewModel.allNotesGroupedBySubject.collectAsState()
    val notes = subjectsMap[subjectName] ?: emptyList<Note>()
    
    var expandedNote by remember { mutableStateOf<Note?>(null) }
    val isHeaderVisible = expandedNote == null

    BackHandler(enabled = expandedNote != null) {
        expandedNote = null
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AnimatedVisibility(
                    visible = isHeaderVisible,
                    enter = fadeIn(tween(300)) + expandVertically(spring(stiffness = Spring.StiffnessLow)),
                    exit = fadeOut(tween(200)) + shrinkVertically(spring(stiffness = Spring.StiffnessLow))
                ) {
                    CenterAlignedTopAppBar(
                        title = { Text(subjectName.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(), 
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay notas en esta materia todavía.", color = Color.Gray)
                    }
                } else {
                    AnimatedVisibility(
                        visible = isHeaderVisible,
                        enter = fadeIn(tween(400)),
                        exit = fadeOut(tween(200))
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            itemsIndexed(notes, key = { _, note -> note.id }) { index, note ->
                                NoteEstheticCard(
                                    note = note,
                                    isEven = index % 2 == 0,
                                    onClick = { expandedNote = note },
                                    onEdit = { onEditNote(note) },
                                    onDelete = { viewModel.deleteNote(note) }
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = !isHeaderVisible,
                        enter = fadeIn(tween(400)) + scaleIn(
                            initialScale = 0.85f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                        exit = fadeOut(tween(250)) + scaleOut(
                            targetScale = 0.85f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            expandedNote?.let { note ->
                                NoteDetailExpandable(
                                    note = note,
                                    onSave = { t, c -> 
                                        viewModel.updateNote(note.id, note.subject, t, c)
                                    },
                                    onClose = { expandedNote = null }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
