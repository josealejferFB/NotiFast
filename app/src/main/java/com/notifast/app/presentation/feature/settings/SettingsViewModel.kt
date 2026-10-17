package com.notifast.app.presentation.feature.settings

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.User
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.AuthStep
import com.notifast.app.domain.repository.CloudAuthException
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.ConvertGradeScaleUseCase
import com.notifast.app.domain.usecase.RescheduleAllAlertsUseCase
import com.notifast.app.data.notification.NotificationSender
import com.notifast.app.domain.usecase.cloud.ObserveLocalSyncSummaryUseCase
import com.notifast.app.domain.util.ErrorSanitizer
import com.notifast.app.presentation.feature.settings.components.CustomStudioTab
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPrefsRepository: UserPrefsRepository,
    private val evaluationRepository: EvaluationRepository,
    private val convertGradeScaleUseCase: ConvertGradeScaleUseCase,
    private val rescheduleAllAlertsUseCase: RescheduleAllAlertsUseCase,
    private val googleAccountRepository: GoogleAccountRepository,
    @param:ApplicationContext private val context: Context,
    private val observeLocalSyncSummaryUseCase: ObserveLocalSyncSummaryUseCase? = null,
    private val notificationSender: NotificationSender? = null
) : ViewModel() {

    private companion object {
        const val TAG = "SettingsViewModel"
        const val ERROR_GRADE_SCALE = "No se pudo actualizar la escala de calificación"
        const val EPSILON = 0.01f
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var profileJob: Job? = null

    init {
        loadProfile()
        observePreferences()
        calculateCacheSize()
        loadConvertibleEvaluationCount()
        observeGoogleAccount()
        observeSyncSummary()
        checkNotificationPermission()
    }

    private fun loadProfile() {
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            val initialUser = authRepository.getUserProfile(userId).getOrNull()
            if (initialUser != null) {
                applyProfileAndAccount(initialUser, _uiState.value.cloudAccount)
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
            runCatching {
                combine(
                    authRepository.observeUserProfile(userId),
                    googleAccountRepository.account
                ) { user, account ->
                    user to account
                }.collect { (user, account) ->
                    applyProfileAndAccount(user ?: initialUser, account)
                }
            }
        }
    }

    private fun applyProfileAndAccount(user: User?, account: CloudAccount?) {
        val nameParts = account?.displayName?.trim()?.split(" ").orEmpty()
        val firstName = user?.firstName?.takeIf { it.isNotBlank() }
            ?: nameParts.firstOrNull().orEmpty()
        val lastName = user?.lastName?.takeIf { it.isNotBlank() }
            ?: nameParts.drop(1).joinToString(" ")
        val email = user?.email?.takeIf { !it.endsWith("@local.notifast") && it.isNotBlank() }
            ?: account?.email.orEmpty()
        val photo = user?.profilePictureUrl?.takeIf { it.isNotBlank() } ?: account?.photoUrl

        _uiState.update {
            it.copy(
                userProfile = user ?: it.userProfile,
                displayFirstName = firstName,
                displayLastName = lastName,
                displayEmail = email,
                displayPhotoUrl = photo,
                cloudAccount = account,
                isLoading = false
            )
        }
    }

    private fun observeSyncSummary() {
        val useCase = observeLocalSyncSummaryUseCase ?: return
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            runCatching {
                useCase(userId).collect { summary ->
                    _uiState.update { it.copy(syncSummary = summary) }
                }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPrefsRepository.isDarkModeFlow.collect { isDark ->
                _uiState.update { it.copy(isDarkMode = isDark) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.hapticsEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(hapticsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.classAlertsEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(classAlertsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.classAlertMinutesFlow.collect { minutes ->
                _uiState.update { it.copy(classAlertMinutes = minutes) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.evalAlertsEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(evalAlertsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.evalAlertDaysFlow.collect { days ->
                _uiState.update { it.copy(evalAlertDays = days) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.dailySummaryEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(dailySummaryEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.defaultGradeScaleFlow.collect { scale ->
                _uiState.update { it.copy(defaultGradeScale = scale) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.minPassingGradeFlow.collect { grade ->
                _uiState.update { it.copy(minPassingGrade = grade) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.activeAcademicCycleFlow.collect { cycle ->
                _uiState.update { it.copy(activeAcademicCycle = cycle) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.firstDayOfWeekFlow.collect { day ->
                _uiState.update { it.copy(firstDayOfWeek = day) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.themePresetIdFlow.collect { presetId ->
                _uiState.update { it.copy(themePresetId = presetId) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.backgroundPresetIdFlow.collect { presetId ->
                _uiState.update { it.copy(backgroundPresetId = presetId) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.customPrimaryHexFlow.collect { hex ->
                _uiState.update { it.copy(customPrimaryHex = hex) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.customBackgroundHexFlow.collect { hex ->
                _uiState.update { it.copy(customBackgroundHex = hex) }
            }
        }
    }

    private fun loadConvertibleEvaluationCount() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            runCatching {
                evaluationRepository.getEvaluations(userId).collect { evals ->
                    val count = evals.count { it.grade != null }
                    _uiState.update { it.copy(evaluationsWithGradeCount = count) }
                }
            }.onFailure { e ->
                Log.e(TAG, "Failed to load evaluations for grade-scale preview", e)
            }
        }
    }

    private fun calculateCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val totalBytes = SettingsCacheSupport.folderSize(context.cacheDir) +
                SettingsCacheSupport.folderSize(context.codeCacheDir)
            val formatted = SettingsCacheSupport.formatBytesToMb(totalBytes)
            _uiState.update { it.copy(cacheSizeBytes = totalBytes, cacheSizeFormatted = formatted) }
        }
    }

    private fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            SettingsCacheSupport.deleteFolderContents(context.cacheDir)
            val remainingBytes = SettingsCacheSupport.folderSize(context.cacheDir)
            val formatted = SettingsCacheSupport.formatBytesToMb(remainingBytes)
            _uiState.update {
                it.copy(
                    cacheSizeBytes = remainingBytes,
                    cacheSizeFormatted = formatted,
                    snackbarMessage = "Caché liberada correctamente"
                )
            }
        }
    }

    private fun changeGradeScale(targetScaleId: String, newPassing: Float) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val currentScale = GradeScale.fromId(_uiState.value.defaultGradeScale)
            val target = GradeScale.fromId(targetScaleId)
            val custom = _uiState.value.minPassingGrade.takeIf { it != 0f }?.toDouble()
            val evals = evaluationRepository.getEvaluations(userId).first()
            val result = convertGradeScaleUseCase(evals, target, currentScale, custom)
            evaluationRepository.updateEvaluations(userId, result.convertedEvaluations).onFailure { e ->
                Log.e(TAG, "Failed to rewrite evaluations on grade scale change", e)
                _uiState.update { s -> s.copy(error = ErrorSanitizer.sanitize(e, ERROR_GRADE_SCALE)) }
                return@launch
            }
            val defaultTargetPassing = target.minPassingGrade.toFloat()
            val userOverrodeSlider = newPassing > 0f &&
                abs(newPassing - defaultTargetPassing) > EPSILON &&
                abs(newPassing - result.minPassingGrade.toFloat()) > EPSILON
            val persistedMinPassing = when {
                userOverrodeSlider -> newPassing
                result.hasCustomMinPassing -> result.minPassingGrade.toFloat()
                else -> 0f
            }
            userPrefsRepository.setDefaultGradeScale(target.id).onFailure { e ->
                Log.e(TAG, "Failed to persist default grade scale", e)
                _uiState.update { s -> s.copy(error = ErrorSanitizer.sanitize(e, ERROR_GRADE_SCALE)) }
                return@launch
            }
            userPrefsRepository.setMinPassingGrade(persistedMinPassing).onFailure { e ->
                Log.e(TAG, "Failed to persist min passing grade", e)
                _uiState.update { s -> s.copy(error = ErrorSanitizer.sanitize(e, ERROR_GRADE_SCALE)) }
                return@launch
            }
            _uiState.update {
                it.copy(
                    defaultGradeScale = target.id,
                    minPassingGrade = persistedMinPassing,
                    lastConvertedCount = result.convertedCount,
                    lastScaleLabel = target.label,
                    error = null
                )
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.LoadProfile -> loadProfile()
            is SettingsEvent.RefreshCacheSize -> calculateCacheSize()
            is SettingsEvent.SetDarkMode -> viewModelScope.launch { userPrefsRepository.setDarkMode(event.enabled) }
            is SettingsEvent.SetHapticsEnabled -> viewModelScope.launch { userPrefsRepository.setHapticsEnabled(event.enabled) }
            is SettingsEvent.SetClassAlertsEnabled -> viewModelScope.launch {
                userPrefsRepository.setClassAlertsEnabled(event.enabled)
                rescheduleAllAlertsUseCase()
            }
            is SettingsEvent.SetClassAlertMinutes -> viewModelScope.launch {
                userPrefsRepository.setClassAlertMinutes(event.minutes)
                rescheduleAllAlertsUseCase()
            }
            is SettingsEvent.SetEvalAlertsEnabled -> viewModelScope.launch {
                userPrefsRepository.setEvalAlertsEnabled(event.enabled)
                rescheduleAllAlertsUseCase()
            }
            is SettingsEvent.SetEvalAlertDays -> viewModelScope.launch {
                userPrefsRepository.setEvalAlertDays(event.days)
                rescheduleAllAlertsUseCase()
            }
            is SettingsEvent.SetDailySummaryEnabled -> viewModelScope.launch {
                userPrefsRepository.setDailySummaryEnabled(event.enabled)
                rescheduleAllAlertsUseCase()
            }
            is SettingsEvent.ChangeGradeScale -> changeGradeScale(event.targetScaleId, event.minPassingGrade)
            is SettingsEvent.SetActiveAcademicCycle -> viewModelScope.launch { userPrefsRepository.setActiveAcademicCycle(event.cycle) }
            is SettingsEvent.SetFirstDayOfWeek -> viewModelScope.launch { userPrefsRepository.setFirstDayOfWeek(event.day) }
            is SettingsEvent.ClearCache -> clearCache()
            is SettingsEvent.OpenDialog -> _uiState.update { it.copy(activeDialog = event.dialog) }
            is SettingsEvent.DismissDialog -> _uiState.update { it.copy(activeDialog = null) }
            is SettingsEvent.OpenCloudHub -> _uiState.update { it.copy(isCloudHubOpen = true) }
            is SettingsEvent.CloseCloudHub -> _uiState.update { it.copy(isCloudHubOpen = false) }
            is SettingsEvent.RefreshNotificationPermission -> checkNotificationPermission()
            is SettingsEvent.UpdateNotificationPermission -> {
                _uiState.update { it.copy(isSystemNotificationPermissionGranted = event.isGranted) }
            }
            is SettingsEvent.OnNotificationPermissionResult -> {
                _uiState.update { it.copy(isSystemNotificationPermissionGranted = event.isGranted) }
                if (event.isGranted) {
                    viewModelScope.launch { rescheduleAllAlertsUseCase() }
                } else if (event.shouldShowRationale) {
                    _uiState.update { it.copy(activeDialog = SettingsDialogType.NOTIFICATION_PERMISSION_RATIONALE) }
                }
            }
            is SettingsEvent.SendTestNotification -> {
                val sent = notificationSender?.sendTestNotification() ?: false
                if (sent) {
                    _uiState.update {
                        it.copy(snackbarMessage = "Notificación de prueba enviada con éxito")
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSystemNotificationPermissionGranted = false,
                            activeDialog = SettingsDialogType.NOTIFICATION_PERMISSION_RATIONALE
                        )
                    }
                }
            }
            is SettingsEvent.ClearSnackbar -> _uiState.update {
                it.copy(snackbarMessage = null, error = null, lastScaleLabel = null)
            }
            is SettingsEvent.Logout -> logout()
            is SettingsEvent.LinkAccount -> linkAccount(event.activityContext)
            is SettingsEvent.SwitchAccount -> switchAccount(event.activityContext)
            is SettingsEvent.UnlinkAccount -> unlinkAccount()
            is SettingsEvent.AuthResult -> handleAuthResult(event.data)
            is SettingsEvent.ClearCloudError -> _uiState.update { it.copy(cloudError = null) }
            is SettingsEvent.SelectThemePreset -> selectThemePreset(event.presetId)
            is SettingsEvent.SelectBackgroundPreset -> selectBackgroundPreset(event.presetId)
            is SettingsEvent.SetCustomPrimaryHex -> setCustomPrimaryHex(event.hex)
            is SettingsEvent.OpenCustomThemeSheet -> openCustomThemeSheet(event.initialTab)
            is SettingsEvent.CloseCustomThemeSheet -> _uiState.update { it.copy(isCustomThemeSheetOpen = false) }
            is SettingsEvent.UpdateCustomDraftColor -> _uiState.update { it.copy(customDraftColorHex = event.hex) }
            is SettingsEvent.UpdateCustomDraftBackgroundPreset -> _uiState.update { it.copy(customDraftBackgroundPresetId = event.presetId) }
            is SettingsEvent.UpdateCustomDraftBackgroundHex -> _uiState.update { it.copy(customDraftBackgroundHex = event.hex) }
            is SettingsEvent.ApplyCustomTheme -> applyCustomTheme()
        }
    }

    private fun selectThemePreset(presetId: String) {
        viewModelScope.launch {
            userPrefsRepository.setThemePresetId(presetId).onFailure { e ->
                Log.e(TAG, "Failed to persist theme preset id", e)
            }
        }
    }

    private fun selectBackgroundPreset(presetId: String) {
        viewModelScope.launch {
            userPrefsRepository.setBackgroundPresetId(presetId).onFailure { e ->
                Log.e(TAG, "Failed to persist background preset id", e)
            }
        }
    }

    private fun setCustomPrimaryHex(hex: String) {
        viewModelScope.launch {
            userPrefsRepository.setCustomPrimaryHex(hex).onFailure { e ->
                Log.e(TAG, "Failed to persist custom primary hex", e)
            }
        }
    }

    private fun openCustomThemeSheet(initialTab: CustomStudioTab = CustomStudioTab.ACCENT) {
        val isDark = _uiState.value.isDarkMode == true
        val activeTheme = com.notifast.app.domain.model.theme.ThemePreset.fromId(_uiState.value.themePresetId)
        val initialDraft = _uiState.value.customPrimaryHex
            ?: if (isDark) activeTheme.primaryDarkHex else activeTheme.primaryLightHex

        val activeBg = com.notifast.app.domain.model.theme.BackgroundPreset.fromId(_uiState.value.backgroundPresetId)
        val initialBgHex = _uiState.value.customBackgroundHex
            ?: if (isDark) activeBg.backgroundDarkHex else activeBg.backgroundLightHex

        _uiState.update {
            it.copy(
                isCustomThemeSheetOpen = true,
                customThemeSheetInitialTab = initialTab,
                customDraftColorHex = initialDraft,
                customDraftBackgroundPresetId = _uiState.value.backgroundPresetId,
                customDraftBackgroundHex = initialBgHex
            )
        }
    }

    private fun applyCustomTheme() {
        viewModelScope.launch {
            val draft = _uiState.value.customDraftColorHex
            val draftBgHex = _uiState.value.customDraftBackgroundHex
            userPrefsRepository.setThemePresetId("custom")
            userPrefsRepository.setCustomPrimaryHex(draft)
            userPrefsRepository.setBackgroundPresetId("custom")
            userPrefsRepository.setCustomBackgroundHex(draftBgHex)
            _uiState.update { it.copy(isCustomThemeSheetOpen = false) }
        }
    }

    private fun checkNotificationPermission() {
        val granted = notificationSender?.areNotificationsEnabled() ?: true
        _uiState.update { it.copy(isSystemNotificationPermissionGranted = granted) }
    }

    private fun observeGoogleAccount() {
        viewModelScope.launch {
            googleAccountRepository.account.collect { account ->
                applyProfileAndAccount(_uiState.value.userProfile, account)
            }
        }
    }

    private fun linkAccount(activityContext: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCloudLoading = true, cloudError = null) }
            googleAccountRepository.signIn(activityContext).fold(
                onSuccess = { step ->
                    when (step) {
                        is AuthStep.NeedsResolution -> _uiState.update {
                            it.copy(pendingAuthIntent = step.pendingIntent, isCloudLoading = false)
                        }
                        is AuthStep.Linked -> _uiState.update { it.copy(isCloudLoading = false) }
                    }
                },
                onFailure = { e ->
                    val isCancelled = ErrorSanitizer.isCancellation(e)
                    _uiState.update {
                        it.copy(
                            isCloudLoading = false,
                            cloudError = if (isCancelled) null else ErrorSanitizer.sanitize(e, "Error al vincular cuenta con Google")
                        )
                    }
                }
            )
        }
    }

    private fun switchAccount(activityContext: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCloudLoading = true, cloudError = null) }
            googleAccountRepository.switchAccount(activityContext).fold(
                onSuccess = { step ->
                    when (step) {
                        is AuthStep.NeedsResolution -> _uiState.update {
                            it.copy(pendingAuthIntent = step.pendingIntent, isCloudLoading = false)
                        }
                        is AuthStep.Linked -> _uiState.update { it.copy(isCloudLoading = false) }
                    }
                },
                onFailure = { e ->
                    val isCancelled = ErrorSanitizer.isCancellation(e)
                    _uiState.update {
                        it.copy(
                            isCloudLoading = false,
                            cloudError = if (isCancelled) null else ErrorSanitizer.sanitize(e, "Error al cambiar de cuenta con Google")
                        )
                    }
                }
            )
        }
    }

    private fun unlinkAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCloudLoading = true, cloudError = null) }
            googleAccountRepository.unlink().fold(
                onSuccess = {
                    _uiState.update { it.copy(isCloudLoading = false, snackbarMessage = "Cuenta desvinculada correctamente") }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isCloudLoading = false, cloudError = ErrorSanitizer.sanitize(e, "Error al desvincular cuenta")) }
                }
            )
        }
    }

    private fun handleAuthResult(data: Intent?) {
        if (data == null) {
            _uiState.update { it.copy(pendingAuthIntent = null, isCloudLoading = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(pendingAuthIntent = null, isCloudLoading = true, cloudError = null) }
            googleAccountRepository.completeAuthorization(data).fold(
                onSuccess = { _uiState.update { it.copy(isCloudLoading = false) } },
                onFailure = { e ->
                    val isCancelled = ErrorSanitizer.isCancellation(e)
                    _uiState.update {
                        it.copy(
                            isCloudLoading = false,
                            cloudError = if (isCancelled) null else ErrorSanitizer.sanitize(e, "Error en autorización con Google")
                        )
                    }
                }
            )
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}

