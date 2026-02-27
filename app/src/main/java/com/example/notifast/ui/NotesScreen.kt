package com.example.notifast.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.model.Note
import com.example.notifast.ui.theme.ColorMateriaNueva
import com.example.notifast.ui.theme.ColorNotaNueva
import com.example.notifast.viewmodel.AuthViewModel
import com.example.notifast.viewmodel.NoteFilter
import com.example.notifast.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notesViewModel: NotesViewModel,
    authViewModel: AuthViewModel,
    onAddNote: () -> Unit,
    onAddSubject: () -> Unit,
    onEditNote: (Note) -> Unit,
    onSubjectClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val notesDisplay by notesViewModel.filteredNotes.collectAsState()
    val currentFilter by notesViewModel.currentFilter.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val userAuth by authViewModel.user.collectAsState()
    val syncMessage by notesViewModel.syncMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
        }
    }

    LaunchedEffect(Unit) {
        notesViewModel.loadNotes()
        notesViewModel.loadSubjects()
        authViewModel.refreshProfile()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth().animateContentSize(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = data.visuals.message, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userProfile?.profilePictureUrl != null) {
                            SubcomposeAsyncImage(
                                model = userProfile?.profilePictureUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = { CircularProgressIndicator(modifier = Modifier.padding(12.dp)) },
                                error = { Icon(Icons.Default.Person, contentDescription = null) }
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        val displayName = if (!userProfile?.firstName.isNullOrBlank()) "${userProfile?.firstName} ${userProfile?.lastName}" else userAuth?.email?.split("@")?.get(0) ?: "Usuario"
                        Text(text = "Hola, $displayName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = userAuth?.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp).clickable { authViewModel.logout(); onLogout() }, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Row {
                    IconButton(onClick = onAddSubject, modifier = Modifier.size(40.dp).background(ColorMateriaNueva, CircleShape)) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onAddNote, modifier = Modifier.size(40.dp).background(ColorNotaNueva, CircleShape)) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Text(
                text = "Mis Notas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { FilterChip(selected = currentFilter == NoteFilter.RECIENTES, onClick = { notesViewModel.setFilter(NoteFilter.RECIENTES) }, label = "Recientes") }
                item { FilterChip(selected = currentFilter == NoteFilter.ANTIGUAS, onClick = { notesViewModel.setFilter(NoteFilter.ANTIGUAS) }, label = "Más Antiguas") }
                item { FilterChip(selected = currentFilter == NoteFilter.POR_MATERIA, onClick = { notesViewModel.setFilter(NoteFilter.POR_MATERIA) }, label = "Por Materia") }
            }

            AnimatedContent(
                targetState = notesDisplay.isEmpty(),
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                label = ""
            ) { isEmpty ->
                if (isEmpty) {
                    // --- EMPTY STATE COLOREADO ---
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                color = ColorMateriaNueva.copy(alpha = 0.15f) // Fondo verde azulado muy suave
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                                    contentDescription = null,
                                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                                    tint = ColorNotaNueva // Icono en rojo corporativo
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Comienza a organizar tus apuntes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Aún no tienes notas. Crea una materia para añadir tu primer apunte.", textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = onAddSubject, 
                                colors = ButtonDefaults.buttonColors(containerColor = ColorMateriaNueva)
                            ) {
                                Icon(Icons.Default.CreateNewFolder, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Crear mi primera Materia")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var noteCounter = 0
                        notesDisplay.forEach { (header, notesList) ->
                            if (currentFilter == NoteFilter.POR_MATERIA) {
                                item(key = "header_$header") {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = header.uppercase(),
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).clickable { onSubjectClick(header) },
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }

                            itemsIndexed(notesList, key = { _, note -> note.id }) { _, note ->
                                NoteEstheticCard(
                                    note = note,
                                    index = noteCounter++,
                                    onEdit = { onEditNote(note) },
                                    onDelete = { notesViewModel.deleteNote(note) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteEstheticCard(
    note: Note,
    index: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isEven = index % 2 == 0
    val baseColor = if (isEven) ColorNotaNueva else ColorMateriaNueva
    val subjectColor = if (isEven) ColorMateriaNueva else ColorNotaNueva

    Box(
        modifier = Modifier
            .width(373.dp)
            .height(208.dp)
            .padding(vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .width(363.dp)
                .height(171.dp)
                .align(if (isEven) Alignment.BottomEnd else Alignment.BottomStart),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = baseColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (isEven) 185.dp else 24.dp,
                        end = if (isEven) 24.dp else 185.dp,
                        top = 20.dp,
                        bottom = 12.dp
                    )
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    horizontalArrangement = Arrangement.End, 
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .width(174.dp)
                .height(202.dp)
                .align(if (isEven) Alignment.TopStart else Alignment.TopEnd),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = subjectColor),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = 0.15f))
                ) {
                    if (note.imageUrl != null) {
                        SubcomposeAsyncImage(
                            model = note.imageUrl,
                            contentDescription = "Foto de la nota",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                }
                            },
                            error = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                                    Icon(Icons.Default.BrokenImage, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
                                    Text("Local", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.3f))
                                }
                            }
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.NoteAdd,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center).size(44.dp),
                            tint = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
                
                Text(
                    text = note.subject,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FilterChip(selected: Boolean, onClick: () -> Unit, label: String) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) ColorNotaNueva else Color.Transparent,
        border = if (selected) null else BorderStroke(1.dp, Color.LightGray)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else Color.Gray
        )
    }
}
