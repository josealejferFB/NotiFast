package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.Note
import com.notifast.app.presentation.atoms.NotiFastChip
import com.notifast.app.presentation.atoms.NotiFastEmptyState

@Composable
fun SubjectNotesTab(
    notes: List<Note>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    availableTags: List<String>,
    selectedTag: String?,
    onTagSelect: (String?) -> Unit,
    subjectName: String,
    subjectColorHex: String = "",
    onAddNoteClick: () -> Unit,
    onNoteClick: (Note) -> Unit,
    onClearFilters: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isFiltering = searchQuery.isNotBlank() || selectedTag != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Input with Clear Button
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    stringResource(R.string.subject_detail_notes_search_placeholder, subjectName),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.btn_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Tags Filter Row
        if (availableTags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NotiFastChip(
                    label = stringResource(R.string.notes_filter_all_tags),
                    selected = selectedTag == null,
                    onClick = { onTagSelect(null) }
                )
                availableTags.forEach { tag ->
                    NotiFastChip(
                        label = "#$tag",
                        selected = selectedTag.equals(tag, ignoreCase = true),
                        onClick = { onTagSelect(if (selectedTag.equals(tag, ignoreCase = true)) null else tag) }
                    )
                }
            }
        }

        // Notes List or Polished Empty States
        if (notes.isEmpty()) {
            if (isFiltering) {
                NotiFastEmptyState(
                    title = stringResource(R.string.subject_detail_notes_filter_empty_title),
                    message = stringResource(R.string.subject_detail_notes_filter_empty_desc),
                    actionLabel = stringResource(R.string.subject_detail_notes_clear_filters),
                    onActionClick = onClearFilters
                )
            } else {
                NotiFastEmptyState(
                    title = stringResource(R.string.subject_detail_notes_empty_title),
                    message = stringResource(R.string.subject_detail_notes_empty_desc),
                    actionLabel = stringResource(R.string.subject_detail_notes_add_btn),
                    onActionClick = onAddNoteClick
                )
            }
        } else {
            notes.forEach { note ->
                SubjectNoteCard(
                    note = note,
                    subjectColorHex = subjectColorHex,
                    onClick = { onNoteClick(note) }
                )
            }
        }
    }
}
