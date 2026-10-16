package com.notifast.app.presentation.feature.addsubject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.feature.addsubject.components.SubjectEditorFormFields
import com.notifast.app.presentation.feature.addsubject.components.SubjectLivePreviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectBottomSheet(
    onDismissRequest: () -> Unit,
    onSubjectCreated: (String) -> Unit,
    viewModel: AddSubjectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            val createdName = uiState.createdSubjectName.ifBlank { uiState.subjectName.trim() }
            viewModel.onEvent(AddSubjectEvent.ResetState)
            onSubjectCreated(createdName)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.add_subject_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.add_subject_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.btn_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SubjectLivePreviewCard(
                subjectName = uiState.subjectName,
                iconName = uiState.selectedIcon,
                hexColor = uiState.selectedColor
            )

            SubjectEditorFormFields(
                subjectName = uiState.subjectName,
                selectedColor = uiState.selectedColor,
                selectedIcon = uiState.selectedIcon,
                onNameChanged = { viewModel.onEvent(AddSubjectEvent.NameChanged(it)) },
                onSuggestionSelected = { viewModel.onEvent(AddSubjectEvent.SuggestionSelected(it)) },
                onColorSelected = { viewModel.onEvent(AddSubjectEvent.ColorSelected(it)) },
                onIconSelected = { viewModel.onEvent(AddSubjectEvent.IconSelected(it)) }
            )

            uiState.error?.let {
                com.notifast.app.presentation.atoms.NotiFastAlertBanner(
                    message = it,
                    onDismiss = { viewModel.onEvent(AddSubjectEvent.ClearError) }
                )
            }

            NotiFastButton(
                text = stringResource(R.string.add_subject_btn_create),
                onClick = { viewModel.onEvent(AddSubjectEvent.SaveClicked) },
                isLoading = uiState.isLoading,
                enabled = uiState.subjectName.isNotBlank() && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }
    }
}
