package com.notifast.app.presentation.feature.studyhub.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.presentation.atoms.NotiFastButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverTemplateEditorSheet(
    draft: CoverDraft,
    subjects: List<SubjectData>,
    isExporting: Boolean,
    onUpdateDraft: ((CoverDraft) -> CoverDraft) -> Unit,
    onSaveLocalDocx: (CoverDraft) -> Unit,
    onExportToGoogleDocs: (CoverDraft) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.study_template_editor_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.delete_subject_cancel)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            if (subjects.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.study_template_field_subject),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { subj ->
                        FilterChip(
                            selected = draft.subjectId == subj.id,
                            onClick = {
                                onUpdateDraft {
                                    it.copy(subjectId = subj.id, subjectName = subj.name)
                                }
                            },
                            label = { Text("${subj.icon} ${subj.name}") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = draft.workTitle,
                onValueChange = { title -> onUpdateDraft { it.copy(workTitle = title) } },
                label = { Text(stringResource(R.string.study_template_field_work_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = draft.subtitle,
                onValueChange = { sub -> onUpdateDraft { it.copy(subtitle = sub) } },
                label = { Text(stringResource(R.string.study_template_field_subtitle)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = draft.authors.joinToString(", "),
                onValueChange = { raw ->
                    val list = raw.split(",").map { a -> a.trim() }.filter { a -> a.isNotBlank() }
                    onUpdateDraft { it.copy(authors = list) }
                },
                label = { Text(stringResource(R.string.study_template_field_authors)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = draft.institutionName,
                onValueChange = { inst -> onUpdateDraft { it.copy(institutionName = inst) } },
                label = { Text(stringResource(R.string.study_template_field_institution)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = draft.teacherName,
                onValueChange = { t -> onUpdateDraft { it.copy(teacherName = t) } },
                label = { Text(stringResource(R.string.study_template_field_teacher)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = draft.formattedDate,
                onValueChange = { d -> onUpdateDraft { it.copy(formattedDate = d) } },
                label = { Text(stringResource(R.string.study_template_field_date)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            CoverMiniPreview(draft = draft)

            Spacer(modifier = Modifier.height(20.dp))

            NotiFastButton(
                text = stringResource(R.string.study_template_btn_save_docx),
                onClick = { onSaveLocalDocx(draft) },
                modifier = Modifier.fillMaxWidth(),
                enabled = draft.workTitle.isNotBlank()
            )
            Spacer(modifier = Modifier.height(10.dp))

            NotiFastButton(
                text = stringResource(R.string.study_template_btn_export_gdocs),
                onClick = { onExportToGoogleDocs(draft) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isExporting,
                enabled = !isExporting && draft.workTitle.isNotBlank(),
                backgroundColor = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
