package com.notifast.app.presentation.feature.notes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.Teacher
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import com.notifast.app.presentation.feature.notes.SubjectInfo

@Composable
fun SubjectsTabContent(
    subjectInfos: List<SubjectInfo>,
    teachers: List<Teacher>,
    onSubjectClick: (String) -> Unit,
    onAddNoteClick: (String) -> Unit,
    onAddEvaluationClick: (String) -> Unit,
    onDeleteSubject: (String) -> Unit,
    onAddSubjectClick: () -> Unit,
    onEditSubject: (SubjectInfo) -> Unit = {},
    onTeacherClick: (Teacher) -> Unit,
    onAddTeacherClick: () -> Unit,
    onWhatsAppClick: ((Teacher) -> Unit)? = null,
    displayScale: GradeScale = GradeScale.SCALE_0_10,
    minPassingGrade: Double = displayScale.minPassingGrade,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Cuerpo Docente (Edge-to-Edge Carousel)
        item(key = "faculty_carousel_section") {
            ProfessorCarouselSection(
                teachers = teachers,
                onTeacherClick = onTeacherClick,
                onAddTeacherClick = onAddTeacherClick,
                onWhatsAppClick = onWhatsAppClick
            )
        }

        // Section 2: Header "Tus Materias" with count badge and action
        item(key = "subjects_header_section") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.notes_section_subjects_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            text = "${subjectInfos.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onAddSubjectClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.notes_section_teachers_add_compact),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Section 3: Subjects Bento Grid or Empty State
        if (subjectInfos.isEmpty()) {
            item(key = "subjects_empty_section") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NotiFastEmptyState(
                        title = stringResource(R.string.subjects_empty_title),
                        message = stringResource(R.string.subjects_empty_message),
                        actionLabel = stringResource(R.string.fab_add_subject),
                        onActionClick = onAddSubjectClick
                    )
                }
            }
        } else {
            val rows = subjectInfos.chunked(2)
            items(
                items = rows,
                key = { row -> row.joinToString("-") { it.name } }
            ) { rowSubjects ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (subjectInfo in rowSubjects) {
                        Box(modifier = Modifier.weight(1f)) {
                            SubjectBentoCard(
                                subjectInfo = subjectInfo,
                                onClick = { onSubjectClick(subjectInfo.name) },
                                onEdit = { onEditSubject(subjectInfo) },
                                onAddNote = { onAddNoteClick(subjectInfo.name) },
                                onAddEval = { onAddEvaluationClick(subjectInfo.name) },
                                onDelete = { onDeleteSubject(subjectInfo.name) },
                                displayScale = displayScale,
                                minPassingGrade = minPassingGrade
                            )
                        }
                    }
                    if (rowSubjects.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
