package com.notifast.app.presentation.feature.schedules.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.domain.model.schedule.UnergImportConflictStrategy
import com.notifast.app.domain.model.schedule.UnergImportPreviewData
import com.notifast.app.presentation.atoms.NotiFastAlertBanner
import com.notifast.app.presentation.atoms.NotiFastButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnergScheduleImportSheet(
    previewData: UnergImportPreviewData,
    conflictStrategy: UnergImportConflictStrategy,
    isImporting: Boolean,
    onDismiss: () -> Unit,
    onToggleSubject: (String) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onSelectStrategy: (UnergImportConflictStrategy) -> Unit,
    onConfirmImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedCount = previewData.subjects.count { it.selected }
    val hasExisting = previewData.subjects.any { it.isExistingSubject }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.unerg_import_sheet_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(
                            R.string.unerg_import_stats_summary,
                            previewData.subjects.size,
                            previewData.totalPresencialHours
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (previewData.studentCi.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = stringResource(R.string.unerg_import_student_ci, previewData.studentCi),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                if (previewData.academicPeriod.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = stringResource(R.string.unerg_import_period, previewData.academicPeriod),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Non-presencial notice
            if (previewData.ignoredNonPresencialCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                NotiFastAlertBanner(
                    message = stringResource(R.string.unerg_import_ignored_notice, previewData.ignoredNonPresencialCount),
                    icon = Icons.Default.Info
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Select all / Deselect all
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Materias a importar ($selectedCount/${previewData.subjects.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { onSelectAll(selectedCount < previewData.subjects.size) }
                ) {
                    Text(
                        text = if (selectedCount == previewData.subjects.size) {
                            stringResource(R.string.unerg_import_deselect_all)
                        } else {
                            stringResource(R.string.unerg_import_select_all)
                        },
                        fontSize = 12.sp
                    )
                }
            }

            // List of subjects
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(previewData.subjects, key = { it.subjectName }) { subject ->
                    UnergImportSubjectItem(
                        subject = subject,
                        onToggleSelected = { onToggleSubject(subject.subjectName) }
                    )
                }

                // Conflict Strategy Section if any existing subject
                if (hasExisting) {
                    item {
                        UnergConflictStrategySelector(
                            conflictStrategy = conflictStrategy,
                            onSelectStrategy = onSelectStrategy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            NotiFastButton(
                text = stringResource(R.string.unerg_import_action_confirm, selectedCount),
                onClick = onConfirmImport,
                isLoading = isImporting,
                enabled = selectedCount > 0 && !isImporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}
