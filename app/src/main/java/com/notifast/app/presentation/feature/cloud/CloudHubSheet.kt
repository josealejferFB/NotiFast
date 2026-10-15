package com.notifast.app.presentation.feature.cloud

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.feature.cloud.components.CloudAccountHeader
import com.notifast.app.presentation.feature.cloud.components.CloudConfirmDialog
import com.notifast.app.presentation.feature.cloud.components.CloudErrorDialog
import com.notifast.app.presentation.feature.cloud.components.CloudProgressOverlay
import com.notifast.app.presentation.feature.cloud.components.CloudSubjectRow
import com.notifast.app.presentation.feature.cloud.components.CloudSummaryCard
import com.notifast.app.presentation.feature.cloud.components.GlobalSettingsRow
import com.notifast.app.presentation.feature.cloud.components.LinkAccountPrompt
import com.notifast.app.presentation.feature.cloud.components.RestoreConflictDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudHubSheet(
    onDismiss: () -> Unit,
    viewModel: CloudViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onEvent(CloudEvent.AuthResult(result.data))
        } else {
            viewModel.onEvent(CloudEvent.AuthResult(null))
        }
    }

    LaunchedEffect(uiState.pendingAuthIntent) {
        val pending = uiState.pendingAuthIntent
        if (pending != null) {
            authLauncher.launch(IntentSenderRequest.Builder(pending).build())
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.cloud_hub_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.cloud_hub_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.btn_close))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.account == null) {
                LinkAccountPrompt(
                    onSignInClick = { viewModel.onEvent(CloudEvent.SignIn(context)) },
                    isLoading = uiState.isLoading
                )
            } else {
                CloudAccountHeader(
                    account = uiState.account!!,
                    lastBackupTimeMillis = uiState.global?.uploadedAt
                )

                if (uiState.isOffline) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.cloud_offline_banner),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (uiState.cloudOnlyBanner > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    NotiFastCard(onClick = { viewModel.onEvent(CloudEvent.SelectAll) }) {
                        Text(
                            text = stringResource(R.string.cloud_banner_found_copies, uiState.cloudOnlyBanner),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (uiState.progress != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CloudProgressOverlay(progress = uiState.progress!!)
                }

                if (uiState.summary != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CloudSummaryCard(
                        summary = uiState.summary!!,
                        onRetry = { viewModel.onEvent(CloudEvent.RetryFailed) },
                        onDismiss = { viewModel.onEvent(CloudEvent.DismissSummary) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    item {
                        GlobalSettingsRow(
                            isSelected = uiState.globalSelected,
                            onToggle = { viewModel.onEvent(CloudEvent.ToggleGlobal) },
                            hasCloudCopy = uiState.global != null
                        )
                    }

                    items(uiState.items, key = { it.subjectId }) { item ->
                        CloudSubjectRow(
                            item = item,
                            isSelected = item.subjectId in uiState.selected,
                            onToggle = { viewModel.onEvent(CloudEvent.Toggle(it)) },
                            onDeleteCloud = { viewModel.onEvent(CloudEvent.DeleteFromCloud(it)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { viewModel.onEvent(CloudEvent.SelectAll) }) {
                        val allSelected = uiState.selected.size == uiState.items.size && uiState.items.isNotEmpty()
                        Text(
                            text = if (allSelected) {
                                stringResource(R.string.cloud_action_deselect_all)
                            } else {
                                stringResource(R.string.cloud_action_select_all)
                            }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NotiFastButton(
                            text = stringResource(R.string.cloud_action_restore),
                            onClick = { viewModel.onEvent(CloudEvent.Restore) },
                            enabled = uiState.canRestore && !uiState.isOffline && !uiState.isLoading,
                            isLoading = false
                        )

                        NotiFastButton(
                            text = stringResource(R.string.cloud_action_upload),
                            onClick = { viewModel.onEvent(CloudEvent.Upload) },
                            enabled = uiState.canUpload && !uiState.isOffline && !uiState.isLoading,
                            isLoading = uiState.isLoading && uiState.progress == null
                        )
                    }
                }
            }
        }
    }

    if (uiState.pendingConflicts.isNotEmpty()) {
        RestoreConflictDialog(
            conflict = uiState.pendingConflicts.first(),
            errorText = uiState.conflictNameError,
            onValidateName = { viewModel.onEvent(CloudEvent.ValidateConflictName(it)) },
            onDecision = { decision, applyToRest ->
                viewModel.onEvent(CloudEvent.ResolveConflict(decision, applyToRest))
            },
            onDismiss = { viewModel.onEvent(CloudEvent.Confirm(false)) }
        )
    }

    if (uiState.pendingConfirm != null) {
        CloudConfirmDialog(
            confirm = uiState.pendingConfirm!!,
            onConfirm = { accepted -> viewModel.onEvent(CloudEvent.Confirm(accepted)) }
        )
    }

    if (uiState.error != null) {
        CloudErrorDialog(
            error = uiState.error!!,
            onDismiss = { viewModel.onEvent(CloudEvent.ClearError) }
        )
    }
}
