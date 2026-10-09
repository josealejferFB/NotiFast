package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastDialog

@Composable
fun NoteEditorDiscardDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    NotiFastDialog(
        title = stringResource(R.string.notes_discard_title),
        message = stringResource(R.string.notes_discard_message),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
