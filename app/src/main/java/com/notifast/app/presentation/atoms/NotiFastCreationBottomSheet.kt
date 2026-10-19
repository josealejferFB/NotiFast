package com.notifast.app.presentation.atoms

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.notifast.app.presentation.theme.color.Ruby
import kotlinx.coroutines.launch

/**
 * Scope receiver provided to the content of [NotiFastCreationBottomSheet].
 */
class CreationSheetScope(
    private val onDismissInternal: (action: (() -> Unit)?) -> Unit,
    private val onRequestDismissInternal: () -> Unit
) {
    /** Smoothly hides the sheet and executes the given action upon finish. */
    fun dismiss(action: (() -> Unit)? = null) = onDismissInternal(action)

    /**
     * Standard dismiss request: if unsaved changes exist, shows the confirmation dialog;
     * otherwise smoothly dismisses the sheet.
     */
    fun requestDismiss() = onRequestDismissInternal()
}

/**
 * NotiFast 3.2 Standard Creation Modal BottomSheet.
 *
 * Encapsulates the institutional 28dp top curvature, semantic surface color,
 * standardized drag handle, statusBars insets, tactile tap-outside keyboard dismiss,
 * and unified BackHandler flow (IME dismiss -> discard confirmation -> sheet dismissal).
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
@Composable
fun NotiFastCreationBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    hasUnsavedChanges: Boolean = false,
    discardTitle: String = stringResource(R.string.schedules_discard_title),
    discardMessage: String = stringResource(R.string.schedules_discard_message),
    discardConfirmText: String = stringResource(R.string.schedules_discard_confirm),
    discardCancelText: String = stringResource(R.string.schedules_discard_cancel),
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { WindowInsets.statusBars },
    content: @Composable CreationSheetScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isImeVisible = WindowInsets.isImeVisible

    var showDiscardDialog by remember { mutableStateOf(false) }

    fun dismissSheet(action: (() -> Unit)? = null) {
        scope.launch {
            runCatching { sheetState.hide() }
            if (action != null) action() else onDismissRequest()
        }
    }

    val requestDismiss = {
        if (hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            dismissSheet()
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
            title = discardTitle,
            message = discardMessage,
            confirmText = discardConfirmText,
            dismissText = discardCancelText,
            confirmColor = Ruby,
            confirmWeight = FontWeight.Bold,
            onConfirm = {
                showDiscardDialog = false
                dismissSheet()
            },
            onDismiss = {
                showDiscardDialog = false
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            requestDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = dragHandle,
        contentWindowInsets = contentWindowInsets,
        modifier = modifier
    ) {
        val sheetScope = remember(scope, sheetState) {
            CreationSheetScope(
                onDismissInternal = { action -> dismissSheet(action) },
                onRequestDismissInternal = { requestDismiss() }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clearFocusOnTapOutside()
        ) {
            sheetScope.content()
        }
    }
}
