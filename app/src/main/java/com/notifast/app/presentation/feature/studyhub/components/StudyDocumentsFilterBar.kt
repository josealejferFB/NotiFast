package com.notifast.app.presentation.feature.studyhub.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.data.model.SubjectData
import com.notifast.app.presentation.atoms.NotiFastChip
import com.notifast.app.presentation.feature.notes.components.SubjectIconMapper
import com.notifast.app.presentation.feature.studyhub.DocTypeFilter

@Composable
fun StudyDocumentsFilterBar(
    subjects: List<SubjectData>,
    selectedSubject: String?,
    selectedDocType: DocTypeFilter,
    docCount: Int,
    onSelectSubject: (String?) -> Unit,
    onSelectDocType: (DocTypeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DocTypeFilter.entries.forEach { filter ->
                    val label = when (filter) {
                        DocTypeFilter.ALL -> stringResource(R.string.study_filter_all)
                        DocTypeFilter.PDF -> stringResource(R.string.study_filter_pdf)
                        DocTypeFilter.DOCX -> stringResource(R.string.study_filter_docx)
                    }
                    NotiFastChip(
                        label = label,
                        selected = selectedDocType == filter,
                        onClick = { onSelectDocType(filter) }
                    )
                }
            }

            // Dynamic Counter Pill
            val counterText = when (docCount) {
                0 -> stringResource(R.string.study_docs_count_none)
                1 -> stringResource(R.string.study_docs_count_single)
                else -> stringResource(R.string.study_docs_count, docCount)
            }
            Text(
                text = counterText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (subjects.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NotiFastChip(
                    label = stringResource(R.string.study_filter_all),
                    selected = selectedSubject == null,
                    onClick = { onSelectSubject(null) }
                )

                subjects.forEach { subj ->
                    val isSelected = selectedSubject.equals(subj.name, ignoreCase = true)
                    NotiFastChip(
                        label = subj.name,
                        selected = isSelected,
                        leadingIcon = SubjectIconMapper.getIcon(subj.icon),
                        onClick = { onSelectSubject(subj.name) }
                    )
                }
            }
        }
    }
}
