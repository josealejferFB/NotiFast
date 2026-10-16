package com.notifast.app.presentation.feature.cloud

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.remote.drive.DriveException
import com.notifast.app.domain.model.cloud.BatchResult
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.model.cloud.CloudSubjectItem
import com.notifast.app.domain.model.cloud.CloudSubjectStatus
import com.notifast.app.domain.model.cloud.ItemOutcome
import com.notifast.app.domain.model.cloud.RestoreDecision
import com.notifast.app.domain.model.cloud.UploadProgress
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.AuthStep
import com.notifast.app.domain.repository.CloudAuthException
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.usecase.ResolveSubjectNameUseCase
import com.notifast.app.domain.usecase.cloud.BuildCloudOverviewUseCase
import com.notifast.app.domain.usecase.cloud.DeleteCloudSubjectUseCase
import com.notifast.app.domain.usecase.cloud.RestoreGlobalDataUseCase
import com.notifast.app.domain.usecase.cloud.RestoreSubjectsUseCase
import com.notifast.app.domain.usecase.cloud.UploadGlobalDataUseCase
import com.notifast.app.domain.usecase.cloud.UploadSubjectsUseCase
import com.notifast.app.domain.util.NameValidation
import com.notifast.app.domain.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CloudViewModel @Inject constructor(
    private val googleAccountRepository: GoogleAccountRepository,
    private val buildCloudOverviewUseCase: BuildCloudOverviewUseCase,
    private val uploadSubjectsUseCase: UploadSubjectsUseCase,
    private val uploadGlobalDataUseCase: UploadGlobalDataUseCase,
    private val restoreSubjectsUseCase: RestoreSubjectsUseCase,
    private val restoreGlobalDataUseCase: RestoreGlobalDataUseCase,
    private val deleteCloudSubjectUseCase: DeleteCloudSubjectUseCase,
    private val resolveSubjectNameUseCase: ResolveSubjectNameUseCase,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private companion object {
        const val TAG = "CloudViewModel"
    }

    private val _uiState = MutableStateFlow(CloudUiState())
    val uiState: StateFlow<CloudUiState> = _uiState.asStateFlow()

    private enum class Operation { UPLOAD, RESTORE }
    private var lastOperation: Operation? = null
    private val currentDecisions = mutableMapOf<String, RestoreDecision>()
    private var pendingRestoreItems = listOf<CloudSubjectItem>()
    private var pendingRestoreGlobal = false

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOffline = !online) }
            }
        }
        viewModelScope.launch {
            googleAccountRepository.account.collect { account ->
                onAccountChanged(account)
            }
        }
    }

    fun onEvent(event: CloudEvent) {
        when (event) {
            is CloudEvent.SignIn -> signIn(event.activity)
            is CloudEvent.AuthResult -> handleAuthResult(event.data)
            is CloudEvent.Toggle -> toggleSelection(event.id)
            is CloudEvent.ToggleGlobal -> toggleGlobal()
            is CloudEvent.SelectAll -> selectAll()
            is CloudEvent.Upload -> handleUpload()
            is CloudEvent.Restore -> handleRestore()
            is CloudEvent.Confirm -> handleConfirm(event.accepted)
            is CloudEvent.ResolveConflict -> resolveConflict(event.decision, event.applyToRest)
            is CloudEvent.DeleteFromCloud -> deleteFromCloud(event.id)
            is CloudEvent.RetryFailed -> retryFailed()
            is CloudEvent.Refresh -> {
                _uiState.update { it.copy(summary = null, failedSubjectIds = emptySet(), error = null) }
                loadOverview()
            }
            is CloudEvent.DismissSummary -> _uiState.update { it.copy(summary = null, failedSubjectIds = emptySet()) }
            is CloudEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is CloudEvent.ValidateConflictName -> validateConflictName(event.candidate)
        }
    }

    private fun onAccountChanged(account: CloudAccount?) {
        if (account == null) {
            _uiState.update {
                it.copy(
                    account = null,
                    items = emptyList(),
                    selected = emptySet(),
                    global = null,
                    globalSelected = false,
                    canUpload = false,
                    canRestore = false,
                    cloudOnlyBanner = 0
                )
            }
        } else {
            _uiState.update { it.copy(account = account, selected = emptySet(), globalSelected = false) }
            loadOverview()
        }
    }

    private fun loadOverview() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = buildCloudOverviewUseCase(uid)
            result.fold(
                onSuccess = { overview ->
                    _uiState.update { current ->
                        val validIds = overview.subjects.map { it.subjectId }.toSet()
                        val prunedSelected = current.selected.filter { it in validIds }.toSet()
                        val cloudOnlyCount = overview.subjects.count { it.status == CloudSubjectStatus.CLOUD_ONLY }
                        current.copy(
                            isLoading = false,
                            items = overview.subjects,
                            global = overview.global,
                            selected = prunedSelected,
                            cloudOnlyBanner = cloudOnlyCount,
                            canUpload = computeCanUpload(prunedSelected, current.globalSelected, overview.subjects),
                            canRestore = computeCanRestore(prunedSelected, current.globalSelected, overview.global, overview.subjects)
                        )
                    }
                },
                onFailure = { e ->
                    Log.e(TAG, "loadOverview failed: ${e.message}", e)
                    _uiState.update { it.copy(isLoading = false, error = mapError(e)) }
                }
            )
        }
    }

    private fun computeCanUpload(selected: Set<String>, globalSelected: Boolean, items: List<CloudSubjectItem>): Boolean {
        if (globalSelected) return true
        val selectedItems = items.filter { it.subjectId in selected }
        return selectedItems.any { it.status != CloudSubjectStatus.CLOUD_ONLY && it.status != CloudSubjectStatus.SYNCED }
    }

    private fun computeCanRestore(
        selected: Set<String>,
        globalSelected: Boolean,
        global: CloudFile?,
        items: List<CloudSubjectItem>
    ): Boolean {
        if (globalSelected && global != null) return true
        val selectedItems = items.filter { it.subjectId in selected }
        return selectedItems.any { it.cloudFile != null || it.status != CloudSubjectStatus.LOCAL_ONLY }
    }

    private fun toggleSelection(id: String) {
        _uiState.update { current ->
            val newSelected = if (id in current.selected) current.selected - id else current.selected + id
            current.copy(
                selected = newSelected,
                failedSubjectIds = current.failedSubjectIds - id,
                summary = null,
                canUpload = computeCanUpload(newSelected, current.globalSelected, current.items),
                canRestore = computeCanRestore(newSelected, current.globalSelected, current.global, current.items)
            )
        }
    }

    private fun toggleGlobal() {
        _uiState.update { current ->
            val newGlobal = !current.globalSelected
            current.copy(
                globalSelected = newGlobal,
                summary = null,
                canUpload = computeCanUpload(current.selected, newGlobal, current.items),
                canRestore = computeCanRestore(current.selected, newGlobal, current.global, current.items)
            )
        }
    }

    private fun selectAll() {
        _uiState.update { current ->
            val newSelected = if (current.selected.size == current.items.size) {
                emptySet()
            } else {
                current.items.map { it.subjectId }.toSet()
            }
            current.copy(
                selected = newSelected,
                summary = null,
                canUpload = computeCanUpload(newSelected, current.globalSelected, current.items),
                canRestore = computeCanRestore(newSelected, current.globalSelected, current.global, current.items)
            )
        }
    }

    private fun handleUpload() {
        if (_uiState.value.isOffline) {
            _uiState.update { it.copy(error = CloudError.OFFLINE) }
            return
        }
        val toUpload = _uiState.value.items.filter {
            it.subjectId in _uiState.value.selected &&
                    it.status != CloudSubjectStatus.CLOUD_ONLY &&
                    it.status != CloudSubjectStatus.SYNCED
        }
        if (toUpload.isEmpty() && !_uiState.value.globalSelected) return

        val hasNewerOrUnlinked = toUpload.any {
            it.status == CloudSubjectStatus.CLOUD_NEWER || it.status == CloudSubjectStatus.BOTH_UNLINKED
        }
        if (hasNewerOrUnlinked) {
            _uiState.update { it.copy(pendingConfirm = CloudConfirm.CloudNewer) }
            return
        }
        checkMeteredAndProceedUpload(toUpload)
    }

    private fun checkMeteredAndProceedUpload(toUpload: List<CloudSubjectItem>) {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            if (networkMonitor.isMetered() && toUpload.isNotEmpty()) {
                val estimatedBytes = uploadSubjectsUseCase.estimateBytes(uid, toUpload)
                if (estimatedBytes > 20 * 1024 * 1024L) {
                    _uiState.update { it.copy(pendingConfirm = CloudConfirm.MobileDataLarge(estimatedBytes)) }
                    return@launch
                }
            }
            executeUpload(toUpload, _uiState.value.globalSelected)
        }
    }

    private fun executeUpload(toUpload: List<CloudSubjectItem>, uploadGlobal: Boolean) {
        lastOperation = Operation.UPLOAD
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true, progress = null, summary = null) }
            val outcomes = mutableListOf<ItemOutcome>()
            try {
                if (toUpload.isNotEmpty()) {
                    uploadSubjectsUseCase(uid, toUpload, context.cacheDir).collect { prog ->
                        when (prog) {
                            is UploadProgress.Progress -> _uiState.update { it.copy(progress = prog) }
                            is UploadProgress.Done -> outcomes.addAll(prog.result.outcomes)
                        }
                    }
                }
                if (uploadGlobal) {
                    val globalResult = uploadGlobalDataUseCase(uid, _uiState.value.global, context.cacheDir)
                    if (globalResult.isSuccess) {
                        outcomes.add(ItemOutcome(id = "global", name = "Perfil y ajustes", error = null))
                    } else {
                        outcomes.add(ItemOutcome(id = "global", name = "Perfil y ajustes", error = globalResult.exceptionOrNull()))
                    }
                }
                val failedIds = outcomes.filter { it.error != null }.map { it.id }.toSet()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        progress = null,
                        summary = BatchResult(outcomes),
                        failedSubjectIds = failedIds
                    )
                }
                loadOverview()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, progress = null, error = mapError(e)) }
            }
        }
    }

    private fun handleRestore() {
        if (_uiState.value.isOffline) {
            _uiState.update { it.copy(error = CloudError.OFFLINE) }
            return
        }
        val toRestore = _uiState.value.items.filter {
            it.subjectId in _uiState.value.selected &&
                    (it.cloudFile != null || it.status != CloudSubjectStatus.LOCAL_ONLY)
        }
        if (toRestore.isEmpty() && (!_uiState.value.globalSelected || _uiState.value.global == null)) return

        if (_uiState.value.globalSelected && _uiState.value.global != null) {
            _uiState.update { it.copy(pendingConfirm = CloudConfirm.RestoreGlobal) }
            return
        }
        checkConflictsAndRestore(toRestore, restoreGlobal = false)
    }

    private fun checkConflictsAndRestore(toRestore: List<CloudSubjectItem>, restoreGlobal: Boolean) {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            pendingRestoreItems = toRestore
            pendingRestoreGlobal = restoreGlobal
            currentDecisions.clear()

            val conflicts = restoreSubjectsUseCase.findConflicts(uid, toRestore)
            if (conflicts.isNotEmpty()) {
                _uiState.update { it.copy(pendingConflicts = conflicts) }
            } else {
                executeRestoreWithDecisions(toRestore, emptyMap(), restoreGlobal)
            }
        }
    }

    private fun handleConfirm(accepted: Boolean) {
        val confirm = _uiState.value.pendingConfirm ?: return
        _uiState.update { it.copy(pendingConfirm = null) }
        if (!accepted) return

        when (confirm) {
            is CloudConfirm.CloudNewer -> {
                val toUpload = _uiState.value.items.filter {
                    it.subjectId in _uiState.value.selected &&
                            it.status != CloudSubjectStatus.CLOUD_ONLY &&
                            it.status != CloudSubjectStatus.SYNCED
                }
                checkMeteredAndProceedUpload(toUpload)
            }
            is CloudConfirm.MobileDataLarge -> {
                val toUpload = _uiState.value.items.filter {
                    it.subjectId in _uiState.value.selected &&
                            it.status != CloudSubjectStatus.CLOUD_ONLY &&
                            it.status != CloudSubjectStatus.SYNCED
                }
                executeUpload(toUpload, _uiState.value.globalSelected)
            }
            is CloudConfirm.RestoreGlobal -> {
                val toRestore = _uiState.value.items.filter {
                    it.subjectId in _uiState.value.selected &&
                            (it.cloudFile != null || it.status != CloudSubjectStatus.LOCAL_ONLY)
                }
                checkConflictsAndRestore(toRestore, restoreGlobal = true)
            }
            is CloudConfirm.DeleteCloud -> {
                executeDeleteFromCloud(confirm.item)
            }
        }
    }

    private fun resolveConflict(decision: RestoreDecision, applyToRest: Boolean) {
        val currentConflict = _uiState.value.pendingConflicts.firstOrNull() ?: return
        val remaining = _uiState.value.pendingConflicts.drop(1)
        currentDecisions[currentConflict.item.subjectId] = decision

        if (applyToRest && (decision is RestoreDecision.Overwrite || decision is RestoreDecision.Skip)) {
            remaining.forEach { conflict ->
                currentDecisions[conflict.item.subjectId] = decision
            }
            _uiState.update { it.copy(pendingConflicts = emptyList(), conflictNameError = null) }
            executeRestoreWithDecisions(pendingRestoreItems, currentDecisions, pendingRestoreGlobal)
        } else {
            if (remaining.isEmpty()) {
                _uiState.update { it.copy(pendingConflicts = emptyList(), conflictNameError = null) }
                executeRestoreWithDecisions(pendingRestoreItems, currentDecisions, pendingRestoreGlobal)
            } else {
                _uiState.update { it.copy(pendingConflicts = remaining, conflictNameError = null) }
            }
        }
    }

    private fun executeRestoreWithDecisions(
        toRestore: List<CloudSubjectItem>,
        decisions: Map<String, RestoreDecision>,
        restoreGlobal: Boolean
    ) {
        lastOperation = Operation.RESTORE
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true, progress = null, summary = null) }
            val outcomes = mutableListOf<ItemOutcome>()
            try {
                if (toRestore.isNotEmpty()) {
                    restoreSubjectsUseCase(uid, toRestore, decisions, context.cacheDir).collect { prog ->
                        when (prog) {
                            is UploadProgress.Progress -> _uiState.update { it.copy(progress = prog) }
                            is UploadProgress.Done -> outcomes.addAll(prog.result.outcomes)
                        }
                    }
                }
                if (restoreGlobal && _uiState.value.global != null) {
                    val globalResult = restoreGlobalDataUseCase(uid, _uiState.value.global!!, context.cacheDir)
                    if (globalResult.isSuccess) {
                        outcomes.add(ItemOutcome(id = "global", name = "Perfil y ajustes", error = null))
                    } else {
                        outcomes.add(ItemOutcome(id = "global", name = "Perfil y ajustes", error = globalResult.exceptionOrNull()))
                    }
                }
                val failedIds = outcomes.filter { it.error != null }.map { it.id }.toSet()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        progress = null,
                        summary = BatchResult(outcomes),
                        failedSubjectIds = failedIds
                    )
                }
                loadOverview()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, progress = null, error = mapError(e)) }
            }
        }
    }

    private fun deleteFromCloud(id: String) {
        val item = _uiState.value.items.firstOrNull { it.subjectId == id } ?: return
        _uiState.update { it.copy(pendingConfirm = CloudConfirm.DeleteCloud(item)) }
    }

    private fun executeDeleteFromCloud(item: CloudSubjectItem) {
        val driveFileId = item.driveFileId ?: item.cloudFile?.driveFileId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = deleteCloudSubjectUseCase.cloudOnly(driveFileId)
            if (res.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
                loadOverview()
            } else {
                _uiState.update { it.copy(isLoading = false, error = mapError(res.exceptionOrNull()!!)) }
            }
        }
    }

    private fun retryFailed() {
        val failed = _uiState.value.summary?.failed ?: return
        val failedIds = failed.map { it.id }.toSet()
        _uiState.update { it.copy(failedSubjectIds = it.failedSubjectIds - failedIds, summary = null) }
        if (lastOperation == Operation.UPLOAD) {
            val toUpload = _uiState.value.items.filter { it.subjectId in failedIds }
            executeUpload(toUpload, uploadGlobal = failedIds.contains("global"))
        } else if (lastOperation == Operation.RESTORE) {
            val toRestore = _uiState.value.items.filter { it.subjectId in failedIds }
            executeRestoreWithDecisions(toRestore, currentDecisions, restoreGlobal = failedIds.contains("global"))
        }
    }

    private fun validateConflictName(candidate: String) {
        val existingNames = _uiState.value.items.map { it.name }
        val validation = resolveSubjectNameUseCase.validate(candidate, existingNames)
        val errorMsg = when (validation) {
            NameValidation.Blank -> "El nombre no puede estar vacío"
            NameValidation.TooLong -> "El nombre no puede superar 50 caracteres"
            NameValidation.Duplicate -> "Ya existe una materia con ese nombre"
            NameValidation.Valid -> null
        }
        _uiState.update { it.copy(conflictNameError = errorMsg) }
    }

    private fun signIn(activityContext: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = googleAccountRepository.signIn(activityContext)
            res.fold(
                onSuccess = { step ->
                    when (step) {
                        is AuthStep.NeedsResolution -> _uiState.update {
                            it.copy(pendingAuthIntent = step.pendingIntent, isLoading = false)
                        }
                        is AuthStep.Linked -> _uiState.update { it.copy(isLoading = false) }
                    }
                },
                onFailure = { e ->
                    Log.e(TAG, "signIn failed: ${e.message}", e)
                    _uiState.update { it.copy(isLoading = false, error = mapError(e)) }
                }
            )
        }
    }

    private fun handleAuthResult(data: android.content.Intent?) {
        viewModelScope.launch {
            _uiState.update { it.copy(pendingAuthIntent = null, isLoading = true, error = null) }
            val res = googleAccountRepository.completeAuthorization(data)
            res.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false) } },
                onFailure = { e ->
                    Log.e(TAG, "completeAuthorization failed: ${e.message}", e)
                    _uiState.update { it.copy(isLoading = false, error = mapError(e)) }
                }
            )
        }
    }

    private fun mapError(t: Throwable): CloudError {
        return when (t) {
            is CloudAuthException -> when (t.reason) {
                CloudAuthException.Reason.CANCELLED -> CloudError.CANCELLED
                CloudAuthException.Reason.NO_PLAY_SERVICES -> CloudError.NO_PLAY_SERVICES
                CloudAuthException.Reason.PERMISSION_DENIED -> CloudError.PERMISSION_DENIED
                CloudAuthException.Reason.REAUTH_REQUIRED -> CloudError.REAUTH_REQUIRED
            }
            is DriveException -> {
                if (t.code == 403 && (t.reason?.contains("quota", ignoreCase = true) == true ||
                            t.reason?.contains("storageQuotaExceeded", ignoreCase = true) == true)) {
                    CloudError.QUOTA_FULL
                } else if (t.code == 401) {
                    CloudError.REAUTH_REQUIRED
                } else {
                    CloudError.GENERIC
                }
            }
            is IOException -> {
                if (_uiState.value.isOffline) CloudError.OFFLINE else CloudError.GENERIC
            }
            else -> CloudError.GENERIC
        }
    }
}
