package com.notifast.app.presentation.feature.studyhub.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastChip
import com.notifast.app.presentation.atoms.NotiFastCreationBottomSheet
import com.notifast.app.presentation.atoms.clearFocusOnTapOutside

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
    val focusManager = LocalFocusManager.current
    val hasUnsavedChanges = draft.workTitle.isNotBlank() || draft.authors.isNotEmpty() || draft.subjectName.isNotBlank()

    NotiFastCreationBottomSheet(
        onDismissRequest = onDismiss,
        hasUnsavedChanges = hasUnsavedChanges
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .clearFocusOnTapOutside()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.study_template_editor_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { requestDismiss() }) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = stringResource(R.string.delete_subject_cancel))
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            if (subjects.isNotEmpty()) {
                Text(text = stringResource(R.string.study_template_field_subject), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    subjects.forEach { subj ->
                        NotiFastChip(
                            label = subj.name,
                            selected = draft.subjectId == subj.id,
                            leadingIcon = com.notifast.app.presentation.feature.notes.components.SubjectIconMapper.getIcon(subj.icon),
                            onClick = {
                                onUpdateDraft {
                                    val unit = if (it.templateType == CoverTemplateType.UNERG_DEGREE) "UNIDAD CURRICULAR: ${subj.name.uppercase()}" else it.curricularUnit
                                    it.copy(subjectId = subj.id, subjectName = subj.name, curricularUnit = unit)
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (draft.templateType == CoverTemplateType.UNERG_DEGREE) {
                UnergMembreteEditorCard(draft = draft, onUpdateDraft = onUpdateDraft)
                Spacer(modifier = Modifier.height(12.dp))
            }

            CoverTypographyPickerCard(draft = draft, onUpdateDraft = onUpdateDraft)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = draft.workTitle,
                onValueChange = { title -> onUpdateDraft { it.copy(workTitle = title) } },
                label = { Text(stringResource(R.string.study_template_field_work_title)) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(6.dp))

            AssistChip(
                onClick = { onUpdateDraft { it.copy(workTitle = it.workTitle.uppercase()) } },
                label = { Text(stringResource(R.string.study_title_uppercase_btn), style = MaterialTheme.typography.labelSmall) },
                leadingIcon = { Icon(Icons.Outlined.FormatSize, null, Modifier.size(16.dp)) },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = draft.subtitle,
                onValueChange = { sub -> onUpdateDraft { it.copy(subtitle = sub) } },
                label = { Text(stringResource(R.string.study_template_field_subtitle)) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            CoverAuthorsEditorCard(draft = draft, onUpdateDraft = onUpdateDraft)
            Spacer(modifier = Modifier.height(12.dp))

            if (draft.templateType != CoverTemplateType.UNERG_DEGREE) {
                OutlinedTextField(
                    value = draft.institutionName,
                    onValueChange = { inst -> onUpdateDraft { it.copy(institutionName = inst) } },
                    label = { Text(stringResource(R.string.study_template_field_institution)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = draft.facultyOrDepartment,
                    onValueChange = { fac -> onUpdateDraft { it.copy(facultyOrDepartment = fac) } },
                    label = { Text(stringResource(R.string.study_template_field_department)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = draft.teacherName,
                    onValueChange = { t -> onUpdateDraft { it.copy(teacherName = t) } },
                    label = { Text(stringResource(R.string.study_template_field_teacher)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                OutlinedTextField(
                    value = draft.teacherName,
                    onValueChange = { t -> onUpdateDraft { it.copy(teacherName = t) } },
                    label = { Text(stringResource(R.string.study_template_field_docente)) },
                    placeholder = { Text(stringResource(R.string.study_template_field_docente_hint)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = draft.tutorName,
                    onValueChange = { tut -> onUpdateDraft { it.copy(tutorName = tut) } },
                    label = { Text(stringResource(R.string.study_template_field_tutor)) },
                    placeholder = { Text(stringResource(R.string.study_template_field_tutor_hint)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = draft.city,
                    onValueChange = { c -> onUpdateDraft { it.copy(city = c) } },
                    label = { Text(stringResource(R.string.study_template_field_city)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = draft.formattedDate,
                    onValueChange = { d -> onUpdateDraft { it.copy(formattedDate = d) } },
                    label = { Text(stringResource(R.string.study_template_field_date)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
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
