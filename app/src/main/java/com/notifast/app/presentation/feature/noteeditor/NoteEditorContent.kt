package com.notifast.app.presentation.feature.noteeditor

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.MarkdownVisualTransformation
import com.notifast.app.presentation.atoms.NotiFastFormField
import com.notifast.app.presentation.feature.noteeditor.components.NoteAttachmentPreview
import com.notifast.app.presentation.feature.noteeditor.components.NoteLivePreviewCard
import com.notifast.app.presentation.feature.noteeditor.components.NoteSubjectSelectorChip
import com.notifast.app.presentation.feature.noteeditor.components.NoteTagsEditor

@Composable
fun NoteEditorContent(
    uiState: NoteEditorUiState,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onSubjectSelected: (String) -> Unit,
    onTagToggled: (String) -> Unit,
    onCustomTagAdded: (String) -> Unit,
    onCustomTagRemoved: (String) -> Unit,
    onInsertFormatting: (prefix: String, suffix: String) -> Unit = { _, _ -> },
    onTogglePreview: (Boolean) -> Unit = {},
    onTaskCheckToggled: (taskIndex: Int, isChecked: Boolean) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onCreateSubjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        NoteSubjectSelectorChip(
            subjects = uiState.subjects,
            selectedSubject = uiState.subject,
            onSubjectSelected = onSubjectSelected,
            onCreateSubjectClick = onCreateSubjectClick
        )

        NoteTagsEditor(
            selectedTags = uiState.tags,
            availableTags = uiState.availableTags,
            onTagToggled = onTagToggled,
            onCustomTagAdded = onCustomTagAdded,
            onCustomTagRemoved = onCustomTagRemoved
        )

        AnimatedContent(
            targetState = uiState.isPreviewMode,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "NoteEditPreviewContent"
        ) { isPreview ->
            if (isPreview) {
                NoteLivePreviewCard(
                    subject = uiState.subject,
                    title = uiState.title,
                    content = uiState.content,
                    tags = uiState.tags,
                    imageUrl = uiState.imageUrl,
                    imageUri = uiState.imageUri,
                    onTaskCheckToggled = onTaskCheckToggled
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    NotiFastFormField(
                        value = uiState.title,
                        onValueChange = onTitleChanged,
                        label = stringResource(R.string.add_note_title_label)
                    )

                    NotiFastFormField(
                        value = uiState.content,
                        onValueChange = onContentChanged,
                        label = stringResource(R.string.add_note_content_label),
                        singleLine = false,
                        minLines = 8,
                        visualTransformation = MarkdownVisualTransformation(MaterialTheme.colorScheme)
                    )

                    NoteAttachmentPreview(
                        existingImageUrl = uiState.imageUrl,
                        imageUri = uiState.imageUri,
                        onImageSelected = onImageSelected
                    )

                    // Stats badge: words + reading time
                    if (uiState.wordCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AutoStories,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.size(6.dp))
                                Text(
                                    text = "${uiState.wordCount} palabras · ~${uiState.readingTimeMinutes} min",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
