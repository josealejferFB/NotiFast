package com.notifast.app.presentation.feature.cloud.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.cloud.RestoreConflict
import com.notifast.app.domain.model.cloud.RestoreDecision
import com.notifast.app.presentation.atoms.NotiFastTextField

private enum class ConflictChoice { OVERWRITE, ADD_AS, SKIP }

@Composable
fun RestoreConflictDialog(
    conflict: RestoreConflict,
    errorText: String?,
    onValidateName: (String) -> Unit,
    onDecision: (decision: RestoreDecision, applyToRest: Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var choice by remember(conflict) { mutableStateOf(ConflictChoice.OVERWRITE) }
    var candidateName by remember(conflict) { mutableStateOf(conflict.suggestedName) }
    var applyToRest by remember(conflict) { mutableStateOf(false) }

    LaunchedEffect(candidateName) {
        onValidateName(candidateName)
    }

    val isConfirmEnabled = when (choice) {
        ConflictChoice.OVERWRITE, ConflictChoice.SKIP -> true
        ConflictChoice.ADD_AS -> errorText == null && candidateName.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.cloud_conflict_title, conflict.item.name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (conflict.hasLocalChanges) {
                    Text(
                        text = stringResource(R.string.cloud_conflict_has_changes_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Overwrite option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            choice = ConflictChoice.OVERWRITE
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = choice == ConflictChoice.OVERWRITE,
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            choice = ConflictChoice.OVERWRITE
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.cloud_conflict_option_overwrite),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Add As option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            choice = ConflictChoice.ADD_AS
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = choice == ConflictChoice.ADD_AS,
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            choice = ConflictChoice.ADD_AS
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.cloud_conflict_option_add_as),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (choice == ConflictChoice.ADD_AS) {
                    Spacer(modifier = Modifier.height(6.dp))
                    NotiFastTextField(
                        value = candidateName,
                        onValueChange = { candidateName = it },
                        label = stringResource(R.string.cloud_conflict_new_name_label),
                        isError = errorText != null,
                        errorMessage = errorText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, end = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Skip option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            choice = ConflictChoice.SKIP
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = choice == ConflictChoice.SKIP,
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            choice = ConflictChoice.SKIP
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.cloud_conflict_option_skip),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Apply to rest (only visible for Overwrite and Skip)
                if (choice != ConflictChoice.ADD_AS) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                applyToRest = !applyToRest
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = applyToRest,
                            onCheckedChange = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                applyToRest = it
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.cloud_conflict_apply_to_rest),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    val decision = when (choice) {
                        ConflictChoice.OVERWRITE -> RestoreDecision.Overwrite
                        ConflictChoice.ADD_AS -> RestoreDecision.AddAs(candidateName.trim())
                        ConflictChoice.SKIP -> RestoreDecision.Skip
                    }
                    onDecision(decision, applyToRest)
                },
                enabled = isConfirmEnabled,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(R.string.btn_accept), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    )
}
