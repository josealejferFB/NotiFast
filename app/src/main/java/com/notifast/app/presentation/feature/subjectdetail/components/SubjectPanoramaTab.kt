package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.data.model.Teacher
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.model.study.StudyDocType
import com.notifast.app.domain.model.study.StudyDocument
import com.notifast.app.domain.usecase.RequiredGradeResult
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.feature.grades.components.GradeSimulatorCard
import com.notifast.app.presentation.feature.subjectdetail.SubjectMilestone
import com.notifast.app.presentation.theme.type.monoAcademicBold

@Composable
fun SubjectPanoramaTab(
    milestone: SubjectMilestone,
    progressPercent: Float,
    completedEvals: Int,
    totalEvals: Int,
    simulationResult: RequiredGradeResult?,
    scale: GradeScale,
    targetGrade: Double,
    onTargetGradeChange: (Double) -> Unit,
    classroom: String,
    teacher: String,
    modality: String,
    teachers: List<Teacher> = emptyList(),
    studyDocuments: List<StudyDocument> = emptyList(),
    onDocumentClick: ((StudyDocument) -> Unit)? = null,
    onTeacherClick: (Teacher) -> Unit = {},
    onWhatsAppClick: ((Teacher) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MilestoneCard(milestone = milestone)

        NotiFastCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.subject_detail_weight_totalizer_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = "$completedEvals / $totalEvals evals", style = MaterialTheme.typography.monoAcademicBold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        GradeSimulatorCard(
            simulationResult = simulationResult,
            scale = scale,
            targetGrade = targetGrade,
            onTargetGradeChange = onTargetGradeChange
        )

        SubjectFacultyCard(
            teachers = teachers,
            legacyTeacherName = teacher,
            classroom = classroom,
            onTeacherClick = onTeacherClick,
            onWhatsAppClick = onWhatsAppClick
        )

        if (studyDocuments.isNotEmpty()) {
            Text(
                text = stringResource(R.string.study_subject_section_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            studyDocuments.forEach { doc ->
                NotiFastCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 12.dp,
                    onClick = { onDocumentClick?.invoke(doc) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isPdf = doc.docType == StudyDocType.PDF
                        val icon = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Description
                        val tint = if (isPdf) Color(0xFFD32F2F) else Color(0xFF1976D2)
                        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = doc.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = doc.fileName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneCard(milestone: SubjectMilestone) {
    NotiFastCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
        when (milestone) {
            is SubjectMilestone.Exam -> {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.subject_detail_next_exam_badge), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        Text(text = milestone.evaluation.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            text = if (milestone.daysRemaining == 0L) "Hoy" else "${milestone.daysRemaining}d",
                            style = MaterialTheme.typography.monoAcademicBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            is SubjectMilestone.Class -> {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.subject_detail_next_class_badge), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(text = "${milestone.dayName} (${milestone.schedule.startTime} - ${milestone.schedule.endTime})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            is SubjectMilestone.None -> {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(text = stringResource(R.string.subject_detail_no_milestones_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = stringResource(R.string.subject_detail_no_milestones_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
