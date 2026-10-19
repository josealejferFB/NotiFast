package com.notifast.app.presentation.feature.studyhub.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.study.StudyDocument
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import com.notifast.app.presentation.atoms.NotiFastTextField
import com.notifast.app.presentation.feature.studyhub.DocTypeFilter

@Composable
fun StudyDocumentsSection(
    documents: List<StudyDocument>,
    subjects: List<SubjectData>,
    selectedSubject: String?,
    selectedDocType: DocTypeFilter,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectSubject: (String?) -> Unit,
    onSelectDocType: (DocTypeFilter) -> Unit,
    onDocumentClick: (StudyDocument) -> Unit,
    onDocumentMoreClick: (StudyDocument) -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Search bar with NotiFastTextField
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            NotiFastTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = stringResource(R.string.nav_study_hub),
                placeholder = stringResource(R.string.study_search_hint),
                leadingIcon = Icons.Outlined.Search,
                singleLine = true
            )
            if (searchQuery.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onSearchQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.study_clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        StudyDocumentsFilterBar(
            subjects = subjects,
            selectedSubject = selectedSubject,
            selectedDocType = selectedDocType,
            docCount = documents.size,
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
                    icon = if (hasFilter) Icons.Outlined.SearchOff else Icons.Outlined.Description,
                    actionLabel = if (!hasFilter) stringResource(R.string.study_btn_import_doc) else null,
                    onActionClick = if (!hasFilter) onImportClick else null
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(documents, key = { it.id }) { doc ->
                    val matchedSubject = subjects.firstOrNull {
                        it.id == doc.subjectId || it.name.equals(doc.subjectName, ignoreCase = true)
                    }
                    StudyDocumentCard(
                        document = doc,
                        subject = matchedSubject,
                        onClick = { onDocumentClick(doc) },
                        onMoreClick = { onDocumentMoreClick(doc) }
                    )
                }
            }
        }
    }
}
