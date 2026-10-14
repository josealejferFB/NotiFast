package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.data.model.Note
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SubjectNotesTab(
    notes: List<Note>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    availableTags: List<String>,
    selectedTag: String?,
    onTagSelect: (String?) -> Unit,
    subjectName: String,
    onAddNoteClick: () -> Unit,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.forLanguageTag("es-ES"))

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(stringResource(R.string.subject_detail_notes_search_placeholder, subjectName), style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        )

        if (availableTags.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedTag == null,
                    onClick = { onTagSelect(null) },
                    label = { Text(stringResource(R.string.notes_filter_all_tags)) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer)
                )
                availableTags.forEach { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { onTagSelect(if (selectedTag == tag) null else tag) },
                        label = { Text("#$tag") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    )
                }
            }
        }

        if (notes.isEmpty()) {
            NotiFastEmptyState(
                title = stringResource(R.string.subject_detail_notes_empty_title),
                message = stringResource(R.string.subject_detail_notes_empty_desc),
                actionLabel = stringResource(R.string.subject_detail_notes_add_btn),
                onActionClick = onAddNoteClick
            )
        } else {
            notes.forEach { note ->
                NotiFastCard(modifier = Modifier.fillMaxWidth(), onClick = { onNoteClick(note) }, contentPadding = 14.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = note.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            note.timestamp?.let { date ->
                                Text(text = dateFormat.format(date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(text = note.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 19.sp)
                        if (note.tags.isNotEmpty() || note.imageUrl != null) {
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                note.tags.take(3).forEach { tag ->
                                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                        Text(text = "#$tag", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                if (note.imageUrl != null) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
