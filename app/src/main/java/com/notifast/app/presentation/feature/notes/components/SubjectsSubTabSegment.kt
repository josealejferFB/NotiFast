package com.notifast.app.presentation.feature.notes.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.feature.notes.NotesSubTab

@Composable
fun SubjectsSubTabSegment(
    selectedSubTab: NotesSubTab,
    onSelectSubTab: (NotesSubTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            val isSubjects = selectedSubTab == NotesSubTab.SUBJECTS
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSubjects) MaterialTheme.colorScheme.primary else Color.Transparent,
                onClick = { onSelectSubTab(NotesSubTab.SUBJECTS) },
                modifier = Modifier.weight(1f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.teachers_subtab_subjects),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSubjects) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSubjects) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val isProfessors = selectedSubTab == NotesSubTab.PROFESSORS
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isProfessors) MaterialTheme.colorScheme.primary else Color.Transparent,
                onClick = { onSelectSubTab(NotesSubTab.PROFESSORS) },
                modifier = Modifier.weight(1f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.teachers_subtab_professors),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isProfessors) FontWeight.Bold else FontWeight.Normal,
                        color = if (isProfessors) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
