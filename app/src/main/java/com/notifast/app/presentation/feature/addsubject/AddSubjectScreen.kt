package com.notifast.app.presentation.feature.addsubject

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.addsubject.components.SubjectEditorFormFields
import com.notifast.app.presentation.feature.addsubject.components.SubjectLivePreviewCard
import com.notifast.app.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectScreen(
    viewModel: AddSubjectViewModel = hiltViewModel(),
    onSubjectCreated: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            val createdName = uiState.createdSubjectName.ifBlank { uiState.subjectName.trim() }
            onSubjectCreated(createdName)
        }
    }

    val hasUnsavedChanges = uiState.subjectName.isNotBlank()
    val handleBack = { if (hasUnsavedChanges) showDiscardDialog = true else onBackClick() }

    BackHandler(enabled = hasUnsavedChanges) { showDiscardDialog = true }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NotiFastTopBar(
                    type = TopBarType.Detail(
                        title = stringResource(R.string.add_subject_title),
                        onBackClick = handleBack
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SubjectLivePreviewCard(
                    subjectName = uiState.subjectName,
                    iconName = uiState.selectedIcon,
                    hexColor = uiState.selectedColor
                )

                NotiFastCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SubjectEditorFormFields(
                            subjectName = uiState.subjectName,
                            selectedColor = uiState.selectedColor,
                            selectedIcon = uiState.selectedIcon,
                            onNameChanged = { viewModel.onEvent(AddSubjectEvent.NameChanged(it)) },
                            onSuggestionSelected = { viewModel.onEvent(AddSubjectEvent.SuggestionSelected(it)) },
                            onColorSelected = { viewModel.onEvent(AddSubjectEvent.ColorSelected(it)) },
                            onIconSelected = { viewModel.onEvent(AddSubjectEvent.IconSelected(it)) }
                        )
                    }
                }

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
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )
            }
        }

        if (showDiscardDialog) {
            NotiFastDialog(
                title = stringResource(R.string.add_subject_discard_title),
                message = stringResource(R.string.add_subject_discard_message),
                confirmText = stringResource(R.string.schedules_discard_confirm),
                dismissText = stringResource(R.string.schedules_discard_cancel),
                confirmColor = Ruby,
                confirmWeight = FontWeight.Bold,
                onConfirm = {
                    showDiscardDialog = false
                    onBackClick()
                },
                onDismiss = { showDiscardDialog = false }
            )
        }
    }
}
