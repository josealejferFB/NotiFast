package com.notifast.app.presentation.feature.addevaluation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastAlertBanner
import com.notifast.app.presentation.atoms.NotiFastCreationBottomSheet
import com.notifast.app.presentation.atoms.clearFocusOnTapOutside

/**
 * NotiFast 3.2 Evaluation Editor Modal BottomSheet Layout.
 * Standardized using [NotiFastCreationBottomSheet] and [clearFocusOnTapOutside].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationEditorSheetLayout(
    isEditing: Boolean,
    isSaved: Boolean,
    hasUnsavedChanges: Boolean,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onCreateSubject: () -> Unit,
    errorMessage: String? = null,
    onErrorDismiss: (() -> Unit)? = null,
    content: @Composable (onCreateSubjectClick: () -> Unit) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val handleCreateSubject = {
        keyboardController?.hide()
        focusManager.clearFocus()
        onCreateSubject()
    }

    NotiFastCreationBottomSheet(
        onDismissRequest = onDismiss,
        hasUnsavedChanges = hasUnsavedChanges,
        discardTitle = stringResource(R.string.evaluation_discard_title),
        discardMessage = stringResource(R.string.evaluation_discard_message),
        discardConfirmText = stringResource(R.string.schedules_discard_confirm),
        discardCancelText = stringResource(R.string.schedules_discard_cancel)
    ) {
        LaunchedEffect(isSaved) {
            if (isSaved) {
                dismiss(action = onSaved)
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            EvaluationEditorSheetHeader(
                isEditing = isEditing,
                onCloseClick = { requestDismiss() }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .clearFocusOnTapOutside()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    NotiFastAlertBanner(
                        message = errorMessage,
                        onDismiss = onErrorDismiss,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                content(handleCreateSubject)

                Spacer(
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                    )
                )
            }
        }
    }
}
