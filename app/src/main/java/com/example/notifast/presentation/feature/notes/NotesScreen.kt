package com.example.notifast.presentation.feature.notes

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.data.model.Note
import com.example.notifast.presentation.atoms.NotiFastCenterAlignedTopBar
import com.example.notifast.presentation.atoms.NotiFastCircularLoading
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.feature.notes.components.NoteEstheticCard
import com.example.notifast.presentation.feature.notes.components.SubjectCard
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    onAddNoteClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onAddEvaluationClick: (String) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var expandedNote by remember { mutableStateOf<Note?>(null) }
    var subjectToDelete by remember { mutableStateOf<String?>(null) }
    val isHeaderVisible = expandedNote == null

    BackHandler(enabled = !isHeaderVisible) { expandedNote = null }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(containerColor = Color.Transparent, topBar = { NotiFastCenterAlignedTopBar(title = "Mis Notas") }) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                AnimatedVisibility(
                    visible = isHeaderVisible,
                    enter = fadeIn(tween(400)),
                    exit = fadeOut(tween(200))
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            HeaderProfile(
                                firstName = uiState.userFirstName,
                                lastName = uiState.userLastName,
                                email = uiState.userEmail,
                                profilePictureUrl = uiState.userProfilePictureUrl,
                                onEditProfile = onEditProfile,
                                onAddSubject = onAddSubjectClick,
                                onAddNote = onAddNoteClick,
                                onLogout = onLogout
                            )
                        }

                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = Color.White,
                                border = if (uiState.searchQuery.isEmpty()) BorderStroke(1.dp, Color.Transparent) else BorderStroke(1.dp, Aqua.copy(alpha = 0.5f)),
                                shadowElevation = 2.dp
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = Aqua, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (uiState.searchQuery.isEmpty()) {
                                            Text("Buscar apuntes o materias...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray.copy(alpha = 0.6f))
                                        }
                                        BasicTextField(
                                            value = uiState.searchQuery,
                                            onValueChange = { viewModel.onEvent(NotesEvent.Search(it)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            cursorBrush = SolidColor(Aqua),
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                                        )
                                    }
                                    if (uiState.searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onEvent(NotesEvent.Search("")) }, modifier = Modifier.size(20.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                NoteFilter.entries.forEach { filter ->
                                    val label = when (filter) { NoteFilter.RECIENTES -> "Recientes"; NoteFilter.ANTIGUAS -> "Más Antiguas"; NoteFilter.POR_MATERIA -> "Por Materia" }
                                    val selected = uiState.currentFilter == filter
                                    item {
                                        Surface(
                                            modifier = Modifier.clickable { viewModel.onEvent(NotesEvent.SetFilter(filter)) },
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (selected) Ruby else Color.White,
                                            border = if (selected) null else BorderStroke(1.dp, Aqua.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = label,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selected) Color.White else Aqua
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (uiState.isLoading) {
                            item { NotiFastCircularLoading() }
                        } else if (uiState.error != null) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Error al cargar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = uiState.error ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(
                                            onClick = { viewModel.onEvent(NotesEvent.LoadNotes) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Ruby)
                                        ) {
                                            Text("Reintentar", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else if (uiState.notes.isEmpty() && uiState.searchQuery.isEmpty() && uiState.rawNotes.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Aún no tienes notas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Crea tu primera materia para comenzar", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(onClick = onAddSubjectClick, colors = ButtonDefaults.buttonColors(containerColor = Aqua)) {
                                            Text("+ Nueva Materia")
                                        }
                                    }
                                }
                            }
                        } else if (uiState.notes.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No hay coincidencias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = Color.Gray)
                                }
                            }
                        } else {
                            var notesOffset = 0
                            uiState.notes.forEach { (header, notesList) ->
                                val listToShow = if (uiState.currentFilter == NoteFilter.RECIENTES) notesList.take(3) else notesList

                                if (uiState.currentFilter == NoteFilter.POR_MATERIA && listToShow.isNotEmpty()) {
                                    item(key = "header_$header") {
                                        val subjectColor = uiState.subjectInfos.find { it.name == header }?.colorHex ?: ""
                                        val color = try { if(subjectColor.isNotBlank()) Color(android.graphics.Color.parseColor(subjectColor)) else Aqua } catch(e: Exception) { Aqua }
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp).clickable { onSubjectClick(header) },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = header.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = color, letterSpacing = 1.sp)
                                        }
                                    }
                                }

                                val currentOffset = notesOffset
                                itemsIndexed(listToShow, key = { _, note -> note.id }) { index, note ->
                                    val subjectColor = uiState.subjectInfos.find { it.name == note.subject }?.colorHex ?: ""
                                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                                        NoteEstheticCard(
                                            note = note,
                                            isEven = (currentOffset + index) % 2 == 0,
                                            onClick = { expandedNote = note },
                                            onEdit = { expandedNote = note },
                                            onDelete = { viewModel.onEvent(NotesEvent.DeleteNote(note)) },
                                            subjectColorHex = subjectColor
                                        )
                                    }
                                }
                                notesOffset += listToShow.size
                            }

                            if (uiState.searchQuery.isEmpty() && uiState.subjectInfos.isNotEmpty()) {
                                item {
                                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            text = "Materias",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                                items(uiState.subjectInfos, key = { it.name }) { info ->
                                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                                        SubjectCard(
                                            info = info,
                                            isExpanded = uiState.expandedSubject == info.name,
                                            onToggle = { viewModel.onEvent(NotesEvent.ToggleSubject(info.name)) },
                                            onAddNote = { onAddNoteClick() },
                                            onAddEvaluation = { subject -> onAddEvaluationClick(subject) },
                                            onNoteClick = { note -> expandedNote = note },
                                            onDeleteSubject = { subjectToDelete = info.name }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !isHeaderVisible,
                    enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)),
                    exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.85f, animationSpec = spring(stiffness = 300f))
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp), contentAlignment = Alignment.Center) {
                        expandedNote?.let { note ->
                            NoteDetailExpandable(
                                note = note,
                                subjectColorHex = uiState.subjectInfos.find { it.name == note.subject }?.colorHex ?: "",
                                onSave = { title, content -> viewModel.onEvent(NotesEvent.UpdateNote(note, title, content)) },
                                onClose = { expandedNote = null }
                            )
                        }
                    }
                }
            }
        }

        subjectToDelete?.let { name ->
            AlertDialog(
                onDismissRequest = { subjectToDelete = null },
                title = { Text("Eliminar materia") },
                text = { Text("¿Estás seguro de eliminar $name? También se borrarán todos los apuntes asociados.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.onEvent(NotesEvent.DeleteSubject(name))
                            subjectToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Ruby)
                    ) { Text("Eliminar", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { subjectToDelete = null }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
private fun NoteDetailExpandable(note: Note, subjectColorHex: String, onSave: (String, String) -> Unit, onClose: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    
    val accentColor = try {
        if (subjectColorHex.isNotBlank()) Color(android.graphics.Color.parseColor(subjectColorHex))
        else Aqua
    } catch(e: Exception) { Aqua }

    Card(
        modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            if (note.imageUrl != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    SubcomposeAsyncImage(model = note.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp), contentScale = ContentScale.Crop)
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                        onClick = { if (isEditing) { onSave(title, content) }; isEditing = !isEditing },
                        shape = CircleShape, color = if (isEditing) accentColor else Color.White
                    ) {
                        Icon(imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null, tint = if(isEditing) Color.White else Color.Gray, modifier = Modifier.padding(8.dp).size(20.dp))
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(60.dp).background(Brush.horizontalGradient(listOf(accentColor, accentColor.copy(alpha = 0.5f)))),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        onClick = { if (isEditing) { onSave(title, content) }; isEditing = !isEditing },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                if (isEditing) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Contenido") }, modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor))
                } else {
                    Text(text = note.subject.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accentColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = note.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = note.content, style = MaterialTheme.typography.bodyMedium, lineHeight = 24.sp, color = Color.DarkGray)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.2f), contentColor = Color.DarkGray)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HeaderProfile(firstName: String, lastName: String, email: String, profilePictureUrl: String?, onEditProfile: () -> Unit, onAddSubject: () -> Unit, onAddNote: () -> Unit, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val displayName = if (firstName.isNotBlank()) firstName else email.split("@").getOrElse(0) { "Usuario" }.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
                
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Aqua).clickable { onEditProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePictureUrl != null) {
                        SubcomposeAsyncImage(model = profilePictureUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text(displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Hola, $displayName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onLogout, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(20.dp)) {
                        Text("Cerrar sesión", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onAddSubject,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Aqua)
            ) {
                Text("+ Materia", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Button(
                onClick = onAddNote,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ruby)
            ) {
                Text("+ Apunte", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
