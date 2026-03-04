package com.example.notifast.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.model.Note
import com.example.notifast.ui.theme.Aqua
import com.example.notifast.ui.theme.Ruby
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
    onEditProfile: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val notesDisplay by notesViewModel.filteredNotes.collectAsState()
    val allSubjectsGrouped by notesViewModel.allNotesGroupedBySubject.collectAsState()
    val currentFilter by notesViewModel.currentFilter.collectAsState()
    val searchQuery by notesViewModel.searchQuery.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val userAuth by authViewModel.user.collectAsState()
    
    var expandedNote by remember { mutableStateOf<Note?>(null) }
    val isHeaderVisible = expandedNote == null

    BackHandler(enabled = expandedNote != null) {
        expandedNote = null
    }

    LaunchedEffect(Unit) {
        notesViewModel.loadNotes()
        notesViewModel.loadSubjects()
        authViewModel.refreshProfile()
    }

    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                // LISTADO PRINCIPAL
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
                        item { HeaderProfile(userProfile, userAuth, onEditProfile, onAddSubject, onAddNote, onLogout, authViewModel) }
                        
                        item {
                            Text(
                                text = "Mis Notas",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }

                        // BARRA DE BUSQUEDA MEJORADA
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(28.dp),
                                color = Color.White.copy(alpha = 0.9f),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = Aqua, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "Buscar apuntes o materias...", 
                                                style = MaterialTheme.typography.bodyMedium, 
                                                color = Color.Gray.copy(alpha = 0.6f)
                                            )
                                        }
                                        BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { text -> notesViewModel.setSearchQuery(text) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            cursorBrush = SolidColor(Aqua),
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                                        )
                                    }
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { notesViewModel.setSearchQuery("") },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { FilterChip(selected = currentFilter == NoteFilter.RECIENTES, onClick = { notesViewModel.setFilter(NoteFilter.RECIENTES) }, label = "Recientes") }
                                item { FilterChip(selected = currentFilter == NoteFilter.ANTIGUAS, onClick = { notesViewModel.setFilter(NoteFilter.ANTIGUAS) }, label = "Más Antiguas") }
                                item { FilterChip(selected = currentFilter == NoteFilter.POR_MATERIA, onClick = { notesViewModel.setFilter(NoteFilter.POR_MATERIA) }, label = "Por Materia") }
                            }
                        }

                        if (notesDisplay.isEmpty()) {
                            item { EmptyStateView(searchQuery, onAddSubject) }
                        } else {
                            // CÁLCULO DE ÍNDICE GLOBAL PARA ZIG-ZAG PERPETUO
                            var notesOffset = 0
                            notesDisplay.forEach { (header, notesList) ->
                                val listToShow = if (currentFilter == NoteFilter.RECIENTES) notesList.take(3) else notesList
                                
                                if (currentFilter == NoteFilter.POR_MATERIA && listToShow.isNotEmpty()) {
                                    item(key = "header_$header") { SubjectHeader(header) { onSubjectClick(header) } }
                                }

                                val currentOffset = notesOffset
                                itemsIndexed(listToShow, key = { _, note -> note.id }) { index, note ->
                                    NoteEstheticCard(
                                        note = note, 
                                        isEven = (currentOffset + index) % 2 == 0,
                                        onClick = { expandedNote = note }, 
                                        onEdit = { onEditNote(note) }, 
                                        onDelete = { notesViewModel.deleteNote(note) }
                                    )
                                }
                                notesOffset += listToShow.size
                            }
                        }

                        if (searchQuery.isEmpty() && allSubjectsGrouped.isNotEmpty()) {
                            item { Text(text = "Materias", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 32.dp, end = 20.dp, bottom = 16.dp)) }
                            item {
                                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                                    allSubjectsGrouped.forEach { (subjectName, notes) ->
                                        item(key = "subject_feat_$subjectName") { SubjectFeaturedCard(subjectName = subjectName, notes = notes, onClick = { onSubjectClick(subjectName) }) }
                                    }
                                }
                            }
                        }
                    }
                }

                // VISTA EXPANDIDA CENTRADA
                AnimatedVisibility(
                    visible = !isHeaderVisible,
                    enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)),
                    exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.85f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 40.dp), contentAlignment = Alignment.Center) {
                        expandedNote?.let { note ->
                            NoteDetailExpandable(note = note, onSave = { t, c -> notesViewModel.updateNote(note.id, note.subject, t, c) }, onClose = { expandedNote = null })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteDetailExpandable(
    note: Note, 
    onSave: (String, String) -> Unit, 
    onClose: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }

    Card(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            if (note.imageUrl != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    SubcomposeAsyncImage(model = note.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().wrapContentHeight(), contentScale = ContentScale.FillWidth)
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                        onClick = { if (isEditing) onSave(title, content); isEditing = !isEditing },
                        shape = CircleShape, color = if (isEditing) Aqua else Ruby
                    ) {
                        Icon(imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp).size(20.dp))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Aqua.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                        onClick = { if (isEditing) onSave(title, content); isEditing = !isEditing },
                        shape = CircleShape, color = if (isEditing) Aqua else Ruby
                    ) {
                        Icon(imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp).size(20.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                if (isEditing) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ruby))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Contenido") }, modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ruby))
                } else {
                    Text(text = note.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = note.content, style = MaterialTheme.typography.bodyLarge, lineHeight = 26.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(32.dp))
                FilledTonalButton(
                    onClick = onClose, modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.filledTonalButtonColors(containerColor = Ruby.copy(alpha = 0.08f), contentColor = Ruby)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finalizar lectura", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun SubjectHeader(header: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).clickable { onClick() }, shape = RoundedCornerShape(12.dp), color = Aqua.copy(alpha = 0.1f)) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = Aqua, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = header.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = Aqua, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun SubjectFeaturedCard(subjectName: String, notes: List<Note>, onClick: () -> Unit) {
    Card(modifier = Modifier.width(280.dp).height(220.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().height(120.dp).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { index ->
                        val note = notes.getOrNull(index)
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(Color.LightGray.copy(alpha = 0.2f))) {
                            if (note?.imageUrl != null) SubcomposeAsyncImage(model = note.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(20.dp), tint = Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = subjectName, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Ruby)
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${notes.size} notas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(text = "ver todas", modifier = Modifier.clickable { onClick() }, style = MaterialTheme.typography.bodySmall, color = Ruby, fontWeight = FontWeight.Bold)
                }
            }
            Surface(modifier = Modifier.align(Alignment.Center).offset(y = (-10).dp).size(56.dp), shape = CircleShape, color = Ruby, shadowElevation = 6.dp) {
                Box(contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp)) }
            }
        }
    }
}

@Composable
fun NoteEstheticCard(note: Note, isEven: Boolean, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val baseColor = if (isEven) Ruby else Aqua
    val subjectColor = if (isEven) Aqua else Ruby
    Box(modifier = Modifier.width(373.dp).height(208.dp).padding(vertical = 8.dp).clickable(interactionSource = interactionSource, indication = null) { onClick() }) {
        Card(modifier = Modifier.width(363.dp).height(171.dp).align(if (isEven) Alignment.BottomEnd else Alignment.BottomStart), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = baseColor)) {
            Column(modifier = Modifier.fillMaxSize().padding(start = if (isEven) 185.dp else 24.dp, end = if (isEven) 24.dp else 185.dp, top = 20.dp, bottom = 12.dp)) {
                Text(text = note.title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(text = note.content, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f), maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) }
                }
            }
        }
        Card(modifier = Modifier.width(174.dp).height(202.dp).align(if (isEven) Alignment.TopStart else Alignment.TopEnd), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = subjectColor), elevation = CardDefaults.cardElevation(10.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp).clip(RoundedCornerShape(18.dp)).background(Color.Black.copy(alpha = 0.15f))) {
                    if (note.imageUrl != null) SubcomposeAsyncImage(model = note.imageUrl, contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(44.dp), tint = Color.White.copy(alpha = 0.3f))
                }
                Text(text = note.subject, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun EmptyStateView(searchQuery: String, onAddSubject: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = Aqua.copy(alpha = 0.15f)) { Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, modifier = Modifier.padding(24.dp).fillMaxSize(), tint = Ruby) }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = if (searchQuery.isEmpty()) "Comienza a organizar tus apuntes" else "No hay coincidencias", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            if (searchQuery.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onAddSubject, colors = ButtonDefaults.buttonColors(containerColor = Aqua)) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear mi primera Materia")
                }
            }
        }
    }
}

@Composable
fun HeaderProfile(userProfile: com.example.notifast.model.User?, userAuth: com.google.firebase.auth.FirebaseUser?, onEditProfile: () -> Unit, onAddSubject: () -> Unit, onAddNote: () -> Unit, onLogout: () -> Unit, viewModel: AuthViewModel) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onEditProfile() }, contentAlignment = Alignment.Center) {
                if (userProfile?.profilePictureUrl != null) SubcomposeAsyncImage(model = userProfile.profilePictureUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                val displayName = if (userProfile?.firstName != null && userProfile.firstName.isNotBlank()) "${userProfile.firstName} ${userProfile.lastName}" else userAuth?.email?.split("@")?.get(0) ?: "Usuario"
                Text(text = "Hola, $displayName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = userAuth?.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp).clickable { viewModel.logout(); onLogout() }, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onAddSubject, shape = RoundedCornerShape(14.dp), color = Aqua, modifier = Modifier.size(width = 46.dp, height = 46.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) } }
            Spacer(modifier = Modifier.width(10.dp))
            Surface(onClick = onAddNote, shape = RoundedCornerShape(14.dp), color = Ruby, modifier = Modifier.size(width = 46.dp, height = 46.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp)) } }
        }
    }
}

@Composable
fun FilterChip(selected: Boolean, onClick: () -> Unit, label: String) {
    Surface(modifier = Modifier.clickable { onClick() }, shape = RoundedCornerShape(20.dp), color = if (selected) Ruby else Color.Transparent, border = if (selected) null else BorderStroke(1.dp, Color.LightGray)) {
        Text(text = label, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) Color.White else Color.Gray)
    }
}
