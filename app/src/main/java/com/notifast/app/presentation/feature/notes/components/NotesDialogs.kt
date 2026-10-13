package com.notifast.app.presentation.feature.notes.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.notifast.app.R
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.Teacher
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.feature.addsubject.AddSubjectBottomSheet
import com.notifast.app.presentation.feature.notes.NotesEvent
import com.notifast.app.presentation.feature.notes.NotesUiState
import com.notifast.app.presentation.feature.notes.NotesViewModel
import com.notifast.app.presentation.feature.notes.SubjectInfo
import com.notifast.app.presentation.feature.share.ImportSubjectBottomSheet

@Composable
fun NotesDialogs(
    uiState: NotesUiState,
    viewModel: NotesViewModel,
    showAddSubjectSheet: Boolean,
    onDismissAddSubjectSheet: () -> Unit,
    onSubjectCreated: (String) -> Unit,
    subjectToEdit: SubjectInfo?,
    onDismissEditSubject: () -> Unit,
    subjectToDelete: String?,
    onDismissDeleteSubject: () -> Unit,
    noteToDelete: Note?,
    onDismissDeleteNote: () -> Unit,
    onDeleteTeacherPrompt: (Teacher) -> Unit,
    teacherToDelete: Teacher?,
    onDismissDeleteTeacher: () -> Unit,
    showImportSubjectSheet: Boolean,
    onDismissImportSubjectSheet: () -> Unit,
    onSubjectImported: (String) -> Unit
) {
    if (showAddSubjectSheet) {
        AddSubjectBottomSheet(
            onDismissRequest = onDismissAddSubjectSheet,
            onSubjectCreated = onSubjectCreated
        )
    }

    subjectToEdit?.let { info ->
        EditSubjectDialog(
            subject = SubjectData(
                name = info.name,
                colorHex = info.colorHex,
                icon = info.icon
            ),
            onDismiss = onDismissEditSubject,
            onSave = { newName, newColor, newIcon ->
                viewModel.onEvent(NotesEvent.UpdateSubject(info.name, newName, newColor, newIcon))
                onDismissEditSubject()
            }
        )
    }

    subjectToDelete?.let { name ->
        NotiFastDialog(
            title = stringResource(R.string.delete_subject_title),
            message = stringResource(R.string.delete_subject_message, name),
            confirmText = stringResource(R.string.delete_subject_confirm),
            dismissText = stringResource(R.string.delete_subject_cancel),
            confirmColor = MaterialTheme.colorScheme.error,
            confirmWeight = FontWeight.Bold,
            onConfirm = {
                viewModel.onEvent(NotesEvent.DeleteSubject(name))
                onDismissDeleteSubject()
            },
            onDismiss = onDismissDeleteSubject
        )
    }

    noteToDelete?.let { note ->
        NotiFastDialog(
            title = stringResource(R.string.delete_note_title),
            message = stringResource(R.string.delete_note_message, note.title),
            confirmText = stringResource(R.string.delete_note_confirm),
            dismissText = stringResource(R.string.delete_note_cancel),
            confirmColor = MaterialTheme.colorScheme.error,
            confirmWeight = FontWeight.Bold,
            onConfirm = {
                viewModel.onEvent(NotesEvent.DeleteNote(note))
                onDismissDeleteNote()
            },
            onDismiss = onDismissDeleteNote
        )
    }

    uiState.selectedTeacherForDetail?.let { teacher ->
        TeacherDetailBottomSheet(
            teacher = teacher,
            onDismiss = { viewModel.onEvent(NotesEvent.SelectTeacherForDetail(null)) },
            onEditClick = { viewModel.onEvent(NotesEvent.OpenAddEditTeacher(it)) },
            onDeleteClick = { onDeleteTeacherPrompt(it) }
        )
    }

    uiState.teacherBeingEdited?.let { teacher ->
        AddEditTeacherBottomSheet(
            teacher = if (teacher.id.isBlank()) null else teacher,
            availableSubjects = uiState.subjects,
            onDismiss = { viewModel.onEvent(NotesEvent.OpenAddEditTeacher(null)) },
            onSave = { viewModel.onEvent(NotesEvent.SaveTeacher(it)) }
        )
    }

    teacherToDelete?.let { teacher ->
        NotiFastDialog(
            title = stringResource(R.string.teacher_delete_title),
            message = stringResource(R.string.teacher_delete_message, teacher.fullName),
            confirmText = stringResource(R.string.btn_delete),
            dismissText = stringResource(R.string.btn_cancel),
            confirmColor = MaterialTheme.colorScheme.error,
            confirmWeight = FontWeight.Bold,
            onConfirm = {
                viewModel.onEvent(NotesEvent.DeleteTeacher(teacher.id))
                onDismissDeleteTeacher()
            },
            onDismiss = onDismissDeleteTeacher
        )
    }

    if (showImportSubjectSheet) {
        ImportSubjectBottomSheet(
            userId = uiState.userId,
            importUseCase = viewModel.importSubjectPackageUseCase,
            onDismiss = onDismissImportSubjectSheet,
            onSubjectImported = onSubjectImported
        )
    }
}
