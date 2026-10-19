package com.notifast.app.presentation.feature.schedules.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.notifast.app.R
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.feature.schedules.SchedulesEvent
import com.notifast.app.presentation.feature.schedules.SchedulesUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesDialogsHost(
    uiState: SchedulesUiState,
    selectedEvaluation: Evaluation?,
    scheduleToDelete: Schedule?,
    evalToDelete: Evaluation?,
    onDismissEvaluationSheet: () -> Unit,
    onEditEvaluationClick: (Evaluation) -> Unit,
    onDeleteEvaluationRequest: (Evaluation) -> Unit,
    onDismissDeleteSchedule: () -> Unit,
    onDismissDeleteEvaluation: () -> Unit,
    onEvent: (SchedulesEvent) -> Unit
) {
    val evalSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Evaluation Details BottomSheet
    if (selectedEvaluation != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissEvaluationSheet,
            sheetState = evalSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AgendaEvaluationBottomSheet(
                eval = selectedEvaluation,
                displayScale = uiState.gradeScale,
                onEdit = {
                    scope.launch { evalSheetState.hide() }.invokeOnCompletion {
                        onDismissEvaluationSheet()
                        onEditEvaluationClick(selectedEvaluation)
                    }
                },
                onDelete = {
                    onDeleteEvaluationRequest(selectedEvaluation)
                    onDismissEvaluationSheet()
                }
            )
        }
    }

    // Delete Schedule Confirmation Dialog
    scheduleToDelete?.let { schedule ->
        NotiFastDialog(
            title = stringResource(R.string.schedules_delete_title),
            message = stringResource(
                R.string.schedules_delete_message,
                schedule.subjectName,
                schedule.startTime,
                schedule.endTime
            ),
            confirmText = stringResource(R.string.btn_delete),
            dismissText = stringResource(R.string.btn_cancel),
            confirmColor = MaterialTheme.colorScheme.error,
            confirmWeight = FontWeight.Bold,
            onConfirm = {
                onEvent(SchedulesEvent.DeleteSchedule(schedule))
                onDismissDeleteSchedule()
            },
            onDismiss = onDismissDeleteSchedule
        )
    }

    // Delete Evaluation Confirmation Dialog
    evalToDelete?.let { eval ->
        NotiFastDialog(
            title = stringResource(R.string.agenda_delete_eval_title),
            message = stringResource(R.string.agenda_delete_eval_message, eval.title),
            confirmText = stringResource(R.string.btn_delete),
            dismissText = stringResource(R.string.btn_cancel),
            confirmColor = MaterialTheme.colorScheme.error,
            confirmWeight = FontWeight.Bold,
            onConfirm = {
                onEvent(SchedulesEvent.DeleteEvaluation(eval))
                onDismissDeleteEvaluation()
            },
            onDismiss = onDismissDeleteEvaluation
        )
    }

    // UNERG Schedule Import Sheet
    if (uiState.isUnergImportSheetVisible && uiState.unergImportPreview != null) {
        UnergScheduleImportSheet(
            previewData = uiState.unergImportPreview,
            conflictStrategy = uiState.unergImportStrategy,
            isImporting = uiState.isImportingPdf,
            onDismiss = { onEvent(SchedulesEvent.DismissUnergImportSheet) },
            onToggleSubject = { onEvent(SchedulesEvent.ToggleUnergSubjectSelected(it)) },
            onSelectAll = { onEvent(SchedulesEvent.SetUnergSelectAll(it)) },
            onSelectStrategy = { onEvent(SchedulesEvent.SetUnergConflictStrategy(it)) },
            onConfirmImport = { onEvent(SchedulesEvent.ConfirmUnergImport) }
        )
    }
}
