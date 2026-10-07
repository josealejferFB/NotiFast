package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.SubjectData

@Composable
fun NoteSubjectSelectorChip(
    subjects: List<SubjectData>,
    selectedSubject: String,
    onSubjectSelected: (String) -> Unit,
    onCreateSubjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        subjects.forEach { subject ->
            val selected = subject.name == selectedSubject
            FilterChip(
                selected = selected,
                onClick = { onSubjectSelected(subject.name) },
                label = { Text(subject.name) },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
        FilterChip(
            selected = false,
            onClick = onCreateSubjectClick,
            label = { Text(stringResource(R.string.notes_create_subject_chip)) },
            shape = RoundedCornerShape(12.dp)
        )
    }
}
