package com.notifast.app.presentation.feature.notes.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            val isSubjects = selectedSubTab == NotesSubTab.SUBJECTS
            val subjectsBgColor by animateColorAsState(
                targetValue = if (isSubjects) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "subjects_bg"
            )
            val subjectsContentColor by animateColorAsState(
                targetValue = if (isSubjects) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 200),
                label = "subjects_content"
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = subjectsBgColor,
                onClick = { onSelectSubTab(NotesSubTab.SUBJECTS) },
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = subjectsContentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.teachers_subtab_subjects),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSubjects) FontWeight.Bold else FontWeight.Medium,
                        color = subjectsContentColor
                    )
                }
            }

            val isProfessors = selectedSubTab == NotesSubTab.PROFESSORS
            val profBgColor by animateColorAsState(
                targetValue = if (isProfessors) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "prof_bg"
            )
            val profContentColor by animateColorAsState(
                targetValue = if (isProfessors) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 200),
                label = "prof_content"
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = profBgColor,
                onClick = { onSelectSubTab(NotesSubTab.PROFESSORS) },
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = profContentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.teachers_subtab_professors),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isProfessors) FontWeight.Bold else FontWeight.Medium,
                        color = profContentColor
                    )
                }
            }
        }
    }
}
