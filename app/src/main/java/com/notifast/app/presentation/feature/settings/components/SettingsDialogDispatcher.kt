package com.notifast.app.presentation.feature.settings.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.feature.settings.SettingsDialogType
import com.notifast.app.presentation.feature.settings.SettingsEvent
import com.notifast.app.presentation.feature.settings.SettingsUiState

@Composable
fun SettingsDialogDispatcher(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit
) {
    when (uiState.activeDialog) {
        SettingsDialogType.THEME -> {
            SettingsThemeDialog(
                currentTheme = uiState.isDarkMode,
                onSelectTheme = { onEvent(SettingsEvent.SetDarkMode(it)) },
                onDismiss = { onEvent(SettingsEvent.DismissDialog) }
            )
        }
        SettingsDialogType.GRADE_SCALE -> {
            SettingsGradeScaleDialog(
                currentScale = uiState.defaultGradeScale,
                currentPassingGrade = uiState.minPassingGrade,
                onConfirm = { scale, passingGrade ->
                    onEvent(SettingsEvent.SetDefaultGradeScale(scale))
                    onEvent(SettingsEvent.SetMinPassingGrade(passingGrade))
                },
                onDismiss = { onEvent(SettingsEvent.DismissDialog) }
            )
        }
        SettingsDialogType.ACADEMIC_CYCLE -> {
            SettingsCycleDialog(
                currentCycle = uiState.activeAcademicCycle,
                onSelectCycle = { onEvent(SettingsEvent.SetActiveAcademicCycle(it)) },
                onDismiss = { onEvent(SettingsEvent.DismissDialog) }
            )
        }
        SettingsDialogType.FIRST_DAY_OF_WEEK -> {
            SettingsFirstDayDialog(
                currentFirstDay = uiState.firstDayOfWeek,
                onSelectFirstDay = { onEvent(SettingsEvent.SetFirstDayOfWeek(it)) },
                onDismiss = { onEvent(SettingsEvent.DismissDialog) }
            )
        }
        SettingsDialogType.CLASS_ALERT_TIMING -> {
            SettingsClassAlertTimingDialog(
                currentMinutes = uiState.classAlertMinutes,
                onSelectMinutes = { onEvent(SettingsEvent.SetClassAlertMinutes(it)) },
                onDismiss = { onEvent(SettingsEvent.DismissDialog) }
            )
        }
        SettingsDialogType.EVAL_ALERT_TIMING -> {
            SettingsEvalAlertTimingDialog(
                currentDays = uiState.evalAlertDays,
                onSelectDays = { onEvent(SettingsEvent.SetEvalAlertDays(it)) },
                onDismiss = { onEvent(SettingsEvent.DismissDialog) }
            )
        }
        SettingsDialogType.ABOUT_BOTTOM_SHEET -> {
            SettingsAboutBottomSheet(
                onDismiss = { onEvent(SettingsEvent.DismissDialog) }
            )
        }
        SettingsDialogType.PRIVACY_POLICY -> {
            SettingsPrivacyDialog(
                onDismiss = { onEvent(SettingsEvent.DismissDialog) }
            )
        }
        SettingsDialogType.LOGOUT -> {
            NotiFastDialog(
                title = stringResource(R.string.settings_dialog_logout_title),
                message = stringResource(R.string.settings_dialog_logout_message),
                confirmText = stringResource(R.string.settings_dialog_logout_confirm),
                dismissText = stringResource(R.string.btn_cancel),
                confirmColor = MaterialTheme.colorScheme.error,
                onConfirm = {
                    onEvent(SettingsEvent.DismissDialog)
                    onEvent(SettingsEvent.Logout)
                },
                onDismiss = { onEvent(SettingsEvent.DismissDialog) }
            )
        }
        SettingsDialogType.EXPORT_DATA, null -> {}
    }
}
