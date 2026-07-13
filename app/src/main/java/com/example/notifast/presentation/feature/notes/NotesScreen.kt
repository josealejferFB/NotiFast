package com.example.notifast.presentation.feature.notes

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.R
import com.example.notifast.data.model.Note
import com.example.notifast.presentation.atoms.NotiFastCircularLoading
import com.example.notifast.presentation.atoms.NotiFastEmptyState
import com.example.notifast.presentation.atoms.NotiFastExpandableFab
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.feature.notes.components.NoteEstheticCard
import com.example.notifast.presentation.feature.notes.components.SubjectCard
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import com.example.notifast.presentation.theme.color.toColorOrAqua
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    onAddNoteClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onAddEvaluationClick: (String) -> Unit,
    onSubjectDetailClick: (String) -> Unit = {},
    onEditProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var subjectToDelete by remember { mutableStateOf<String?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var expandedNote by remember { mutableStateOf<Note?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                com.example.notifast.presentation.atoms.NotiFastTopBar(
                    type = com.example.notifast.presentation.atoms.TopBarType.Home(
                        firstName = uiState.userFirstName,
                        email = uiState.userEmail,
                        profilePictureUrl = uiState.userProfilePictureUrl,
                        onProfileClick = onEditProfile
                    )
                )
            },
            floatingActionButton = {
                NotiFastExpandableFab(
                    actions = listOf(
                        com.example.notifast.presentation.atoms.FabAction(
                            label = stringResource(R.string.fab_add_subject),
                            icon = Icons.Default.CreateNewFolder,
                            onClick = onAddSubjectClick,
                            contentDescription = "Añadir Materia",
                            color = Aqua
                        ),
                        com.example.notifast.presentation.atoms.FabAction(
                            label = stringResource(R.string.fab_add_note),
                            icon = Icons.Default.NoteAdd,
                            onClick = onAddNoteClick,
                            contentDescription = "Añadir Apunte",
                            color = Ruby
                        )
                    ),
                    modifier = Modifier.padding(bottom = 72.dp)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = Ruby,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = Ruby
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("Apuntes", fontWeight = if(selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium) }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Materias", fontWeight = if(selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium) }
                        )
                    }

                    if (selectedTabIndex == 0) {
                        NotesTabContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            onSubjectClick = onSubjectClick,
                            onNoteClick = onNoteClick,
                            onExpandNote = { expandedNote = it },
                            onDeleteNote = { noteToDelete = it }
                        )
                    } else {
                        SubjectsTabContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            onAddNoteClick = onAddNoteClick,
                            onAddEvaluationClick = onAddEvaluationClick,
                            onNoteClick = onNoteClick,
                            onDeleteSubject = { subjectToDelete = it },
                            onSubjectDetailClick = onSubjectDetailClick
                        )
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

        noteToDelete?.let { note ->
            AlertDialog(
                onDismissRequest = { noteToDelete = null },
                title = { Text(stringResource(R.string.delete_note_title)) },
                text = { Text("¿Estás seguro de eliminar el apunte '${note.title}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.onEvent(NotesEvent.DeleteNote(note))
                            noteToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Ruby)
                    ) { Text(stringResource(R.string.delete_note_confirm), color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { noteToDelete = null }) { Text(stringResource(R.string.delete_note_cancel)) }
                }
            )
        }

        AnimatedVisibility(
            visible = expandedNote != null,
            enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)),
            exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.85f, animationSpec = spring(stiffness = 300f))
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 40.dp), contentAlignment = Alignment.Center) {
                expandedNote?.let { note ->
                    NoteDetailExpandable(
                        note = note,
                        onClose = { expandedNote = null },
                        onEdit = { onNoteClick(note.id); expandedNote = null }
                    )
                }
            }
        }
    }
}



@Composable
private fun NotesTabContent(
    uiState: NotesUiState,
    viewModel: NotesViewModel,
    onSubjectClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onExpandNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            val borderColor by animateColorAsState(
                targetValue = if (uiState.searchQuery.isEmpty()) Aqua.copy(alpha = 0.4f) else Aqua,
                label = "search_border"
            )
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(if (uiState.searchQuery.isEmpty()) 1.dp else 2.dp, borderColor)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Aqua, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (uiState.searchQuery.isEmpty()) {
                            Text(stringResource(R.string.notes_search_placeholder), style = MaterialTheme.typography.bodyMedium, color = Color.Gray.copy(alpha = 0.6f))
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
            LazyRow(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            item { ErrorState(uiState.error) { viewModel.onEvent(NotesEvent.LoadNotes) } }
        } else if (uiState.notes.isEmpty() && uiState.searchQuery.isEmpty() && uiState.rawNotes.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
                    NotiFastEmptyState(
                        icon = Icons.Default.NoteAdd,
                        title = "Aún no tienes apuntes",
                        message = "Empieza creando tu primera materia para poder añadir apuntes y organizar tu estudio."
                    )
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
                        val color = subjectColor.toColorOrAqua()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp).clickable { onSubjectClick(header) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = header.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = color, letterSpacing = 1.sp)
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
                            onClick = { onExpandNote(note) },
                            onEdit = { onNoteClick(note.id) },
                            onDelete = { onDeleteNote(note) },
                            subjectColorHex = subjectColor
                        )
                    }
                }
                notesOffset += listToShow.size
            }
        }
    }
}

@Composable
private fun SubjectsTabContent(
    uiState: NotesUiState,
    viewModel: NotesViewModel,
    onAddNoteClick: () -> Unit,
    onAddEvaluationClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onDeleteSubject: (String) -> Unit,
    onSubjectDetailClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.subjectInfos.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
                    NotiFastEmptyState(
                        icon = Icons.Default.CreateNewFolder,
                        title = "Sin materias",
                        message = "Crea tu primera materia usando el botón inferior."
                    )
                }
            }
        } else {
            items(uiState.subjectInfos, key = { it.name }) { info ->
                Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                    SubjectCard(
                        info = info,
                        isExpanded = uiState.expandedSubject == info.name,
                        onToggle = { viewModel.onEvent(NotesEvent.ToggleSubject(info.name)) },
                        onAddNote = { onAddNoteClick() },
                        onAddEvaluation = { subject -> onAddEvaluationClick(subject) },
                        onNoteClick = { note -> onNoteClick(note.id) },
                        onDeleteSubject = { onDeleteSubject(info.name) },
                        onSubjectDetail = { onSubjectDetailClick(info.name) }
                    )
                }
            }
        }
    }
}



@Composable
private fun ErrorState(error: String?, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.general_error), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error ?: "",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Ruby)) {
                Text("Reintentar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NoteDetailExpandable(note: Note, onClose: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            if (note.imageUrl != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    SubcomposeAsyncImage(model = note.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = note.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = note.content, style = MaterialTheme.typography.bodyLarge, lineHeight = 26.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onClose,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("Cerrar", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Ruby)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
