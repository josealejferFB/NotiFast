package com.notifast.app.presentation.feature.notes.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.data.model.Note
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import com.notifast.app.presentation.feature.notes.NoteFilter
import com.notifast.app.presentation.feature.notes.NotesEvent
import com.notifast.app.presentation.feature.notes.NotesUiState
import com.notifast.app.presentation.feature.notes.NotesViewModel
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby
import com.notifast.app.presentation.theme.color.toColorOrAqua
import java.util.Locale

@Composable
fun NotesTabContent(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(if (uiState.searchQuery.isEmpty()) 1.dp else 2.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Aqua,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (uiState.searchQuery.isEmpty()) {
                            Text(
                                stringResource(R.string.notes_search_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
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
                        IconButton(
                            onClick = { viewModel.onEvent(NotesEvent.Search("")) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(NoteFilter.entries.toList()) { filter ->
                    val label = when (filter) {
                        NoteFilter.RECIENTES -> stringResource(R.string.notes_filter_recent)
                        NoteFilter.ANTIGUAS -> stringResource(R.string.notes_filter_oldest)
                        NoteFilter.POR_MATERIA -> stringResource(R.string.notes_filter_by_subject)
                    }
                    val selected = uiState.currentFilter == filter
                    Surface(
                        modifier = Modifier.clickable { viewModel.onEvent(NotesEvent.SetFilter(filter)) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) Ruby else MaterialTheme.colorScheme.surface,
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

        item {
            NoteTagFilterRow(
                availableTags = uiState.availableTags,
                selectedTag = uiState.selectedTag,
                onSelectTag = { viewModel.onEvent(NotesEvent.SelectTag(it)) }
            )
        }

        if (uiState.isLoading) {
            item { NotiFastCircularLoading() }
        } else if (uiState.error != null) {
            item {
                ErrorState(uiState.error) { viewModel.onEvent(NotesEvent.LoadNotes) }
            }
        } else if (uiState.notes.isEmpty() && uiState.searchQuery.isEmpty() && uiState.rawNotes.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
                    NotiFastEmptyState(
                        icon = Icons.AutoMirrored.Filled.NoteAdd,
                        title = stringResource(R.string.notes_empty_title),
                        message = stringResource(R.string.notes_empty_message)
                    )
                }
            }
        } else if (uiState.notes.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.notes_no_matches),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                .clickable { onSubjectClick(header) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = header.uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = color,
                                letterSpacing = 1.sp
                            )
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
private fun ErrorState(error: String?, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.general_error),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
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
