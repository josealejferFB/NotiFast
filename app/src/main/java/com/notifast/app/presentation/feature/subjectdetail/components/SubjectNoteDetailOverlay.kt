package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.notifast.app.data.model.Note
import com.notifast.app.presentation.feature.notes.components.NoteDetailExpandable

@Composable
fun SubjectNoteDetailOverlay(
    note: Note?,
    onDismiss: () -> Unit,
    onEditNote: (Note) -> Unit,
    onTaskCheckedChange: ((taskIndex: Int, isChecked: Boolean) -> Unit)? = null
) {
    BackHandler(enabled = note != null) {
        onDismiss()
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = note != null,
        enter = fadeIn(tween(250)) + scaleIn(animationSpec = spring(dampingRatio = 0.65f, stiffness = 320f), initialScale = 0.88f),
        exit = fadeOut(tween(200)) + scaleOut(animationSpec = spring(stiffness = 320f), targetScale = 0.88f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.48f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(horizontal = 22.dp, vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            note?.let { currentNote ->
                Box(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                ) {
                    NoteDetailExpandable(
                        note = currentNote,
                        onClose = onDismiss,
                        onEdit = {
                            onDismiss()
                            onEditNote(currentNote)
                        },
                        onTaskCheckedChange = onTaskCheckedChange
                    )
                }
            }
        }
    }
}
