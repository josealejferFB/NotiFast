package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.notifast.app.R
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.Teacher
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.usecase.ExportSubjectPackageUseCase
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.feature.notes.components.EditSubjectDialog
import com.notifast.app.presentation.feature.notes.components.TeacherDetailBottomSheet
import com.notifast.app.presentation.feature.share.ShareSubjectBottomSheet

@Composable
fun SubjectDetailDialogs(
    gradeDialogEval: Evaluation?,
    onDismissGradeDialog: () -> Unit,
    onSaveGrade: (Double?, Boolean) -> Unit,
    gradeScale: GradeScale,
    deleteEval: Evaluation?,
    onDismissDeleteEval: () -> Unit,
    onConfirmDeleteEval: (String) -> Unit,
    deleteSchedule: Schedule?,
    onDismissDeleteSchedule: () -> Unit,
    onConfirmDeleteSchedule: (Schedule) -> Unit,
    showShareSheet: Boolean,
    onDismissShareSheet: () -> Unit,
    userId: String,
    subjectName: String,
    subjectColorHex: String,
    subjectIcon: String,
    exportUseCase: ExportSubjectPackageUseCase,
    showEditDialog: Boolean,
    onDismissEditDialog: () -> Unit,
    isRenaming: Boolean,
    onSaveSubjectData: (String, String, String, String) -> Unit,
    subjectSection: String,
    selectedTeacher: Teacher?,
    onDismissTeacherDetail: () -> Unit
) {
    gradeDialogEval?.let { eval ->
        SubjectGradeQuickDialog(
            evaluation = eval,
            scale = gradeScale,
            onSave = { grade, isPresented ->
                onSaveGrade(grade, isPresented)
                onDismissGradeDialog()
            },
            onDismiss = onDismissGradeDialog
        )
    }

    deleteEval?.let { eval ->
        NotiFastDialog(
            title = stringResource(R.string.subject_detail_delete_eval_title),
            message = stringResource(R.string.subject_detail_delete_eval_msg, eval.title),
            confirmText = stringResource(R.string.btn_delete),
            dismissText = stringResource(R.string.btn_cancel),
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = {
                onConfirmDeleteEval(eval.id)
                onDismissDeleteEval()
            },
            onDismiss = onDismissDeleteEval
        )
    }

    deleteSchedule?.let { schedule ->
        NotiFastDialog(
            title = stringResource(R.string.subject_detail_delete_schedule_title),
            message = stringResource(
                R.string.subject_detail_delete_schedule_msg,
                Schedule.DAY_NAMES.getOrElse(schedule.dayOfWeek) { "" },
                schedule.startTime,
                schedule.endTime
            ),
            confirmText = stringResource(R.string.btn_delete),
            dismissText = stringResource(R.string.btn_cancel),
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = {
                onConfirmDeleteSchedule(schedule)
                onDismissDeleteSchedule()
            },
            onDismiss = onDismissDeleteSchedule
        )
    }

    if (showShareSheet) {
        ShareSubjectBottomSheet(
            userId = userId,
            subjectName = subjectName,
            subjectColorHex = subjectColorHex,
            subjectIcon = subjectIcon,
            exportUseCase = exportUseCase,
            onDismiss = onDismissShareSheet
        )
    }

    if (showEditDialog) {
        EditSubjectDialog(
            subject = SubjectData(
                name = subjectName,
                colorHex = subjectColorHex,
                icon = subjectIcon,
                section = subjectSection
            ),
            isSaving = isRenaming,
            onDismiss = onDismissEditDialog,
            onSave = onSaveSubjectData
        )
    }

    selectedTeacher?.let { teacher ->
        TeacherDetailBottomSheet(
            teacher = teacher,
            onDismiss = onDismissTeacherDetail,
            onEditClick = { onDismissTeacherDetail() },
            onDeleteClick = { onDismissTeacherDetail() }
        )
    }
}
