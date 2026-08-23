package com.notifast.app.presentation.feature.addnote.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.SubjectData
import com.notifast.app.presentation.atoms.NotiFastFormField

@Composable
fun NoteEditorFormFields(
    subjects: List<SubjectData>,
    selectedSubject: String,
    title: String,
    content: String,
    tags: List<String>,
    imageUri: Uri?,
    onSubjectSelected: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onTagToggled: (String) -> Unit,
    onAddCustomTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onCreateSubjectClick: () -> Unit,
    contentHeight: Dp = 140.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        NoteSubjectSelectorChip(
            subjects = subjects,
            selectedSubject = selectedSubject,
            onSubjectSelected = onSubjectSelected,
            onCreateSubjectClick = onCreateSubjectClick
        )

        NotiFastFormField(
            value = title,
            onValueChange = onTitleChanged,
            label = stringResource(R.string.add_note_title_label)
        )

        NoteMarkdownToolbar(
            onInsertFormatting = { prefix, suffix ->
                onContentChanged(content + "$prefix$suffix")
            }
        )

        NotiFastFormField(
            value = content,
            onValueChange = onContentChanged,
            label = stringResource(R.string.add_note_content_label),
            height = contentHeight,
            singleLine = false
        )

        NoteTagsFlowRow(
            selectedTags = tags,
            onTagToggled = onTagToggled,
            onAddCustomTag = onAddCustomTag,
            onRemoveTag = onRemoveTag
        )

        NoteAttachmentPreview(
            imageUri = imageUri,
            onImageSelected = onImageSelected
        )
    }
}
