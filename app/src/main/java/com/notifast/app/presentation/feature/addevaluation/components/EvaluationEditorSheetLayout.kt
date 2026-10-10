package com.notifast.app.presentation.feature.addevaluation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.theme.color.Ruby
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
@Composable
fun EvaluationEditorSheetLayout(
    isEditing: Boolean,
    isSaved: Boolean,
    hasUnsavedChanges: Boolean,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onCreateSubject: () -> Unit,
    content: @Composable (onCreateSubjectClick: () -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isImeVisible = WindowInsets.isImeVisible

    var showDiscardDialog by remember { mutableStateOf(false) }
    var pendingDismissAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun dismissSheet(action: (() -> Unit)? = null) {
        scope.launch {
            runCatching { sheetState.hide() }
            if (action != null) action() else onDismiss()
        }
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            dismissSheet(action = onSaved)
        }
    }

    BackHandler(enabled = true) {
        when {
            isImeVisible -> {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
            hasUnsavedChanges -> showDiscardDialog = true
            else -> dismissSheet()
        }
    }

    if (showDiscardDialog) {
        NotiFastDialog(
            title = stringResource(R.string.evaluation_discard_title),
            message = stringResource(R.string.evaluation_discard_message),
            confirmText = stringResource(R.string.schedules_discard_confirm),
            dismissText = stringResource(R.string.schedules_discard_cancel),
            confirmColor = Ruby,
            confirmWeight = FontWeight.Bold,
            onConfirm = {
                showDiscardDialog = false
                val pending = pendingDismissAction
                pendingDismissAction = null
                dismissSheet(action = pending)
            },
            onDismiss = {
                showDiscardDialog = false
                pendingDismissAction = null
            }
        )
    }

    val handleCreateSubject = {
        keyboardController?.hide()
        focusManager.clearFocus()
        if (hasUnsavedChanges) {
            pendingDismissAction = onCreateSubject
            showDiscardDialog = true
        } else {
            dismissSheet(action = onCreateSubject)
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (hasUnsavedChanges) showDiscardDialog = true else onDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        contentWindowInsets = { WindowInsets.statusBars }
    ) {
        val sheetView = androidx.compose.ui.platform.LocalView.current
        val onTapOutsideSheet: () -> Unit = remember(focusManager, keyboardController, sheetView) {
            {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                val imm = sheetView.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                imm?.hideSoftInputFromWindow(sheetView.windowToken, 0)
                Unit
            }
        }
        val tapOutsideModifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onTapOutsideSheet() }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(tapOutsideModifier)
        ) {
            EvaluationEditorSheetHeader(
                isEditing = isEditing,
                onCloseClick = {
                    onTapOutsideSheet()
                    if (hasUnsavedChanges) showDiscardDialog = true else dismissSheet()
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
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
