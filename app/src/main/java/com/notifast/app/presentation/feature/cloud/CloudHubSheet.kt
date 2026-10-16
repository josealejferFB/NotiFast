package com.notifast.app.presentation.feature.cloud

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.domain.model.cloud.CloudSubjectStatus
import com.notifast.app.presentation.atoms.AlertSeverity
import com.notifast.app.presentation.atoms.LocalNotiFastSnackbarHostState
import com.notifast.app.presentation.atoms.showNotiFastSnackbar
import com.notifast.app.presentation.feature.cloud.components.CloudActionBar
import com.notifast.app.presentation.feature.cloud.components.CloudAccountHeader
import com.notifast.app.presentation.feature.cloud.components.CloudDialogsHost
import com.notifast.app.presentation.feature.cloud.components.CloudFilter
import com.notifast.app.presentation.feature.cloud.components.CloudFilterBar
import com.notifast.app.presentation.feature.cloud.components.CloudSheetBanners
import com.notifast.app.presentation.feature.cloud.components.CloudSheetHeader
import com.notifast.app.presentation.feature.cloud.components.CloudSubjectList
import com.notifast.app.presentation.feature.cloud.components.LinkAccountPrompt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudHubSheet(
    onDismiss: () -> Unit,
    viewModel: CloudViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalNotiFastSnackbarHostState.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentFilter by rememberSaveable { mutableStateOf(CloudFilter.ALL) }

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val data = if (result.resultCode == Activity.RESULT_OK) result.data else null
        viewModel.onEvent(CloudEvent.AuthResult(data))
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(CloudEvent.Refresh)
    }

    LaunchedEffect(uiState.pendingAuthIntent) {
        uiState.pendingAuthIntent?.let {
            authLauncher.launch(IntentSenderRequest.Builder(it).build())
        }
    }

    LaunchedEffect(uiState.summary) {
        val summary = uiState.summary
        if (summary != null && summary.failed.isEmpty()) {
            delay(5000L)
            viewModel.onEvent(CloudEvent.DismissSummary)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { err ->
            val errorMsg = when (err) {
                CloudError.CANCELLED -> context.getString(R.string.cloud_error_cancelled)
                CloudError.NO_PLAY_SERVICES -> context.getString(R.string.cloud_error_no_play_services)
                CloudError.PERMISSION_DENIED -> context.getString(R.string.cloud_error_permission_denied)
                CloudError.REAUTH_REQUIRED -> context.getString(R.string.cloud_error_reauth_required)
                CloudError.QUOTA_FULL -> context.getString(R.string.cloud_error_quota_full)
                CloudError.OFFLINE -> context.getString(R.string.cloud_error_offline)
                CloudError.GENERIC -> context.getString(R.string.cloud_error_generic)
            }
            scope.launch {
                snackbarHostState.showNotiFastSnackbar(
                    message = errorMsg,
                    severity = AlertSeverity.ERROR,
                    title = context.getString(R.string.cloud_hub_title)
                )
            }
        }
    }

    val allCount by remember(uiState.items) { derivedStateOf { uiState.items.size } }
    val pendingCount by remember(uiState.items) {
        derivedStateOf {
            uiState.items.count {
                it.status == CloudSubjectStatus.LOCAL_CHANGES ||
                it.status == CloudSubjectStatus.LOCAL_ONLY ||
                it.status == CloudSubjectStatus.CLOUD_NEWER
            }
        }
    }
    val syncedCount by remember(uiState.items) {
        derivedStateOf { uiState.items.count { it.status == CloudSubjectStatus.SYNCED } }
    }
    val conflictsCount by remember(uiState.items, uiState.failedSubjectIds) {
        derivedStateOf {
            uiState.items.count {
                it.status == CloudSubjectStatus.BOTH_UNLINKED || it.subjectId in uiState.failedSubjectIds
            }
        }
    }

    val filteredItems by remember(uiState.items, currentFilter, uiState.failedSubjectIds) {
        derivedStateOf {
            when (currentFilter) {
                CloudFilter.ALL -> uiState.items
                CloudFilter.PENDING -> uiState.items.filter {
                    it.status == CloudSubjectStatus.LOCAL_CHANGES ||
                    it.status == CloudSubjectStatus.LOCAL_ONLY ||
                    it.status == CloudSubjectStatus.CLOUD_NEWER
                }
                CloudFilter.SYNCED -> uiState.items.filter {
                    it.status == CloudSubjectStatus.SYNCED
                }
                CloudFilter.CONFLICTS -> uiState.items.filter {
                    it.status == CloudSubjectStatus.BOTH_UNLINKED || it.subjectId in uiState.failedSubjectIds
                }
            }
        }
    }

    val handleDismiss = {
        viewModel.onEvent(CloudEvent.DismissSummary)
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = handleDismiss,
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
            CloudSheetHeader(onDismiss = handleDismiss)

            Spacer(modifier = Modifier.height(12.dp))

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

                CloudSheetBanners(
                    isOffline = uiState.isOffline,
                    cloudOnlyBanner = uiState.cloudOnlyBanner,
                    isLoadingScanning = uiState.isLoading && uiState.progress == null && uiState.items.isEmpty(),
                    progress = uiState.progress,
                    summary = uiState.summary,
                    onSelectAllCopies = { viewModel.onEvent(CloudEvent.SelectAll) },
                    onRetrySummary = { viewModel.onEvent(CloudEvent.RetryFailed) },
                    onDismissSummary = { viewModel.onEvent(CloudEvent.DismissSummary) }
                )

                if (uiState.items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    CloudFilterBar(
                        currentFilter = currentFilter,
                        onFilterSelected = { currentFilter = it },
                        allCount = allCount,
                        pendingCount = pendingCount,
                        syncedCount = syncedCount,
                        conflictsCount = conflictsCount
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                CloudSubjectList(
                    filteredItems = filteredItems,
                    hasAnyItems = uiState.items.isNotEmpty(),
                    showGlobalRow = currentFilter == CloudFilter.ALL,
                    isGlobalSelected = uiState.globalSelected,
                    hasGlobalCloudCopy = uiState.global != null,
                    selectedIds = uiState.selected,
                    failedIds = uiState.failedSubjectIds,
                    onToggleGlobal = { viewModel.onEvent(CloudEvent.ToggleGlobal) },
                    onToggleSubject = { viewModel.onEvent(CloudEvent.Toggle(it)) },
                    onDeleteCloudSubject = { viewModel.onEvent(CloudEvent.DeleteFromCloud(it)) },
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val selectedCount = uiState.selected.size + (if (uiState.globalSelected) 1 else 0)
                val allSelected = uiState.selected.size == uiState.items.size && uiState.items.isNotEmpty()

                CloudActionBar(
                    allSelected = allSelected,
                    selectedCount = selectedCount,
                    canRestore = uiState.canRestore,
                    canUpload = uiState.canUpload,
                    isOffline = uiState.isOffline,
                    isLoading = uiState.isLoading && uiState.progress == null,
                    onToggleSelectAll = { viewModel.onEvent(CloudEvent.SelectAll) },
                    onRestore = { viewModel.onEvent(CloudEvent.Restore) },
                    onUpload = { viewModel.onEvent(CloudEvent.Upload) }
                )
            }
        }
    }

    CloudDialogsHost(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}
