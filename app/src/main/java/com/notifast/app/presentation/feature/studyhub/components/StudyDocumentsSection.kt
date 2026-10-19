package com.notifast.app.presentation.feature.studyhub.components

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.study.StudyDocument
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import com.notifast.app.presentation.feature.studyhub.DocTypeFilter

@Composable
fun StudyDocumentsSection(
    documents: List<StudyDocument>,
    subjects: List<SubjectData>,
    selectedSubject: String?,
    selectedDocType: DocTypeFilter,
    searchQuery: String,
    onSelectSubject: (String?) -> Unit,
    onSelectDocType: (DocTypeFilter) -> Unit,
    onDocumentClick: (StudyDocument) -> Unit,
    onDocumentMoreClick: (StudyDocument) -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        FilterBar(
            subjects = subjects,
            selectedSubject = selectedSubject,
            selectedDocType = selectedDocType,
            onSelectSubject = onSelectSubject,
            onSelectDocType = onSelectDocType
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                val hasFilter = !selectedSubject.isNullOrBlank() || selectedDocType != DocTypeFilter.ALL || searchQuery.isNotBlank()
                NotiFastEmptyState(
                    title = stringResource(R.string.study_empty_docs_title),
                    message = if (hasFilter) {
                        stringResource(R.string.study_empty_filter_docs_msg)
                    } else {
                        stringResource(R.string.study_empty_docs_msg)
                    },
                    icon = Icons.Default.Description,
                    actionLabel = stringResource(R.string.study_btn_import_doc),
                    onActionClick = onImportClick
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(documents, key = { it.id }) { doc ->
                    StudyDocumentCard(
                        document = doc,
                        onClick = { onDocumentClick(doc) },
                        onMoreClick = { onDocumentMoreClick(doc) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterBar(
    subjects: List<SubjectData>,
    selectedSubject: String?,
    selectedDocType: DocTypeFilter,
    onSelectSubject: (String?) -> Unit,
    onSelectDocType: (DocTypeFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DocTypeFilter.entries.forEach { filter ->
                val label = when (filter) {
                    DocTypeFilter.ALL -> stringResource(R.string.study_filter_all)
                    DocTypeFilter.PDF -> stringResource(R.string.study_filter_pdf)
                    DocTypeFilter.DOCX -> stringResource(R.string.study_filter_docx)
                }
                FilterChip(
                    selected = selectedDocType == filter,
                    onClick = { onSelectDocType(filter) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        if (subjects.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedSubject == null,
                    onClick = { onSelectSubject(null) },
                    label = { Text(stringResource(R.string.study_filter_all)) }
                )
                subjects.forEach { subj ->
                    FilterChip(
                        selected = selectedSubject.equals(subj.name, ignoreCase = true),
                        onClick = { onSelectSubject(subj.name) },
                        label = { Text("${subj.icon} ${subj.name}") }
                    )
                }
            }
        }
    }
}
