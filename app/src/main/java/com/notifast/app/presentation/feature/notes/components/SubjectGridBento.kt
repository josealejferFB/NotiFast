package com.notifast.app.presentation.feature.notes.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import com.notifast.app.presentation.feature.notes.SubjectInfo
import com.notifast.app.presentation.theme.ThemeUtils
import com.notifast.app.presentation.theme.color.ForestSage
import com.notifast.app.presentation.theme.color.MutedCrimson
import com.notifast.app.presentation.theme.color.AquaGreen
import com.notifast.app.presentation.theme.color.toSafeComposeColor
import com.notifast.app.presentation.theme.type.monoAcademic
import com.notifast.app.presentation.util.GradeFormatters

@Composable
fun SubjectGridBento(
    subjectInfos: List<SubjectInfo>,
    onSubjectClick: (String) -> Unit,
    onAddNoteClick: (String) -> Unit,
    onAddEvaluationClick: (String) -> Unit,
    onDeleteSubject: (String) -> Unit,
    onAddSubjectClick: () -> Unit,
    displayScale: GradeScale = GradeScale.SCALE_0_10,
    minPassingGrade: Double = displayScale.minPassingGrade,
    modifier: Modifier = Modifier
) {
    if (subjectInfos.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            NotiFastEmptyState(
                title = stringResource(R.string.subjects_empty_title),
                message = stringResource(R.string.subjects_empty_message),
                actionLabel = stringResource(R.string.fab_add_subject),
                onActionClick = onAddSubjectClick
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(subjectInfos, key = { it.name }) { subjectInfo ->
                SubjectBentoCard(
                    subjectInfo = subjectInfo,
                    onClick = { onSubjectClick(subjectInfo.name) },
                    onAddNote = { onAddNoteClick(subjectInfo.name) },
                    onAddEval = { onAddEvaluationClick(subjectInfo.name) },
                    onDelete = { onDeleteSubject(subjectInfo.name) },
                    displayScale = displayScale,
                    minPassingGrade = minPassingGrade
                )
            }
        }
    }
}

@Composable
private fun SubjectBentoCard(
    subjectInfo: SubjectInfo,
    onClick: () -> Unit,
    onAddNote: () -> Unit,
    onAddEval: () -> Unit,
    onDelete: () -> Unit,
    displayScale: GradeScale,
    minPassingGrade: Double
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "subject_bento_scale"
    )

    val rawColor = subjectInfo.colorHex.toSafeComposeColor(default = MutedCrimson)
    val accentColor = remember(rawColor, isDark) { ThemeUtils.getAccessibleColor(rawColor, isDark) }

    Card(
        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }.clickable(interactionSource = interactionSource, indication = null) { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = accentColor.copy(alpha = 0.16f), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(subjectInfo.name.take(2).uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = accentColor)
                    }
                }
                subjectInfo.averageGrade?.let { avg ->
                    Text(
                        GradeFormatters.average(avg, displayScale),
                        style = MaterialTheme.typography.monoAcademic,
                        fontWeight = FontWeight.Bold,
                        color = if (avg >= minPassingGrade) ForestSage else AquaGreen
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(subjectInfo.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.notes_bento_subject_notes, subjectInfo.notes.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (subjectInfo.evaluations.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                val progress = subjectInfo.completedEvalCount.toFloat() / subjectInfo.evaluations.size.toFloat()
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(4.dp), color = accentColor, trackColor = MaterialTheme.colorScheme.surfaceContainerHigh, strokeCap = StrokeCap.Round)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.notes_bento_subject_evals, subjectInfo.completedEvalCount, subjectInfo.evaluations.size), style = MaterialTheme.typography.monoAcademic, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onAddNote, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.AutoMirrored.Filled.NoteAdd, stringResource(R.string.notes_bento_add_note), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.width(2.dp))
                IconButton(onClick = onAddEval, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.PostAdd, stringResource(R.string.notes_bento_add_eval), tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.width(2.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, stringResource(R.string.btn_delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}
