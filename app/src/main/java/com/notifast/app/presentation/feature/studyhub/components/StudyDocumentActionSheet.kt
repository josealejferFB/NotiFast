package com.notifast.app.presentation.feature.studyhub.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.study.StudyDocument
import com.notifast.app.presentation.atoms.NotiFastDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyDocumentActionSheet(
    document: StudyDocument,
    onDismiss: () -> Unit,
    onPreview: () -> Unit,
    onOpenExternal: () -> Unit,
    onDuplicate: () -> Unit,
    onRequestAssignSubject: () -> Unit,
    onShare: () -> Unit,
    onRequestDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = document.fileName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            ActionItem(
                icon = Icons.Default.Visibility,
                label = stringResource(R.string.study_doc_action_preview),
                onClick = { onDismiss(); onPreview() }
            )
            ActionItem(
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                label = stringResource(R.string.study_doc_action_open_external),
                onClick = { onDismiss(); onOpenExternal() }
            )
            ActionItem(
                icon = Icons.Default.ContentCopy,
                label = stringResource(R.string.study_doc_action_duplicate),
                onClick = { onDismiss(); onDuplicate() }
            )
            ActionItem(
                icon = Icons.Default.Book,
                label = stringResource(R.string.study_doc_action_assign_subject),
                onClick = { onDismiss(); onRequestAssignSubject() }
            )
            ActionItem(
                icon = Icons.Default.Share,
                label = stringResource(R.string.study_doc_action_share),
                onClick = { onDismiss(); onShare() }
            )
            ActionItem(
                icon = Icons.Default.Delete,
                label = stringResource(R.string.study_doc_action_delete),
                tint = MaterialTheme.colorScheme.error,
                onClick = { onDismiss(); onRequestDelete() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(color = tint)
        )
    }
}

@Composable
fun AssignSubjectDialog(
    subjects: List<SubjectData>,
    currentSubjectName: String,
    onDismiss: () -> Unit,
    onSelectSubject: (SubjectData?) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.study_doc_action_assign_subject)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSubject(null) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sin materia asignada",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }
                subjects.forEach { subj ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSubject(subj) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${subj.icon} ${subj.name}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (subj.name.equals(currentSubjectName, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.delete_subject_cancel))
            }
        }
    )
}
