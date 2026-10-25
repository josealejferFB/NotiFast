package com.notifast.app.presentation.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.model.cloud.LocalSyncSummary
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.cloud.ObserveLocalSyncSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class SplashUiState(
    val isAppReady: Boolean = false,
    val isDarkMode: Boolean? = null,
    val hapticsEnabled: Boolean = true,
    val themePresetId: String = "classic",
    val backgroundPresetId: String = "classic",
    val customPrimaryHex: String? = null,
    val customBackgroundHex: String? = null,
    val authState: Boolean? = null
)

private data class ThemePreferencesState(
    val themePresetId: String,
    val backgroundPresetId: String,
    val customPrimaryHex: String?,
    val customBackgroundHex: String?
)

data class MainTopBarState(
    val firstName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val syncSummary: LocalSyncSummary = LocalSyncSummary.NO_ACCOUNT
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    userPrefsRepository: UserPrefsRepository,
    private val googleAccountRepository: GoogleAccountRepository,
    private val observeLocalSyncSummaryUseCase: ObserveLocalSyncSummaryUseCase
) : ViewModel() {
    val authState: StateFlow<Boolean?> = authRepository.authState

    val splashState: StateFlow<SplashUiState> = combine(
        combine(
            authRepository.authState,
            userPrefsRepository.isDarkModeFlow,
            userPrefsRepository.hapticsEnabledFlow
        ) { auth, isDark, haptics ->
            Triple(auth, isDark, haptics)
        },
        combine(
            userPrefsRepository.themePresetIdFlow,
            userPrefsRepository.backgroundPresetIdFlow,
            userPrefsRepository.customPrimaryHexFlow,
            userPrefsRepository.customBackgroundHexFlow
        ) { theme, bg, primaryHex, bgHex ->
            ThemePreferencesState(theme, bg, primaryHex, bgHex)
        }
    ) { (auth, isDark, haptics), themePrefs ->
        SplashUiState(
            isAppReady = auth != null,
            isDarkMode = isDark,
            hapticsEnabled = haptics,
            themePresetId = themePrefs.themePresetId,
            backgroundPresetId = themePrefs.backgroundPresetId,
            customPrimaryHex = themePrefs.customPrimaryHex,
            customBackgroundHex = themePrefs.customBackgroundHex,
            authState = auth
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SplashUiState()
    )

    val isAppReady: StateFlow<Boolean> = splashState
        .map { it.isAppReady }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val isDarkMode: StateFlow<Boolean?> = splashState
        .map { it.isDarkMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val hapticsEnabled: StateFlow<Boolean> = splashState
        .map { it.hapticsEnabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val themePresetId: StateFlow<String> = splashState
        .map { it.themePresetId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "classic")

    val backgroundPresetId: StateFlow<String> = splashState
        .map { it.backgroundPresetId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "classic")

    val customPrimaryHex: StateFlow<String?> = splashState
        .map { it.customPrimaryHex }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val customBackgroundHex: StateFlow<String?> = splashState
        .map { it.customBackgroundHex }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _pendingDeepLink = MutableStateFlow<NotificationDeepLink?>(null)
    val pendingDeepLink: StateFlow<NotificationDeepLink?> = _pendingDeepLink.asStateFlow()

    private val _topBarState = MutableStateFlow(MainTopBarState())
    val topBarState: StateFlow<MainTopBarState> = _topBarState.asStateFlow()

    init {
        observeUserProfileAndSync()
    }

    private fun observeUserProfileAndSync() {
        viewModelScope.launch {
            authRepository.authState.collectLatest { isAuthenticated ->
                if (isAuthenticated == true) {
                    val uid = authRepository.getCurrentUserId() ?: return@collectLatest
                    launch {
                        combine(
                            authRepository.observeUserProfile(uid),
                            googleAccountRepository.account
                        ) { user, account ->
                            user to account
                        }.collect { (user, account) ->
                            val firstName = user?.firstName?.takeIf { it.isNotBlank() }
                                ?: account?.displayName?.split(" ")?.firstOrNull().orEmpty()
                            val email = user?.email?.takeIf { !it.endsWith("@local.notifast") && it.isNotBlank() }
                                ?: account?.email.orEmpty()
                            val photo = user?.profilePictureUrl?.takeIf { it.isNotBlank() } ?: account?.photoUrl

                            _topBarState.update { it.copy(
                                firstName = firstName,
                                email = email,
                                photoUrl = photo
                            ) }
                        }
                    }
                    launch {
                        observeLocalSyncSummaryUseCase(uid).collect { summary ->
                            _topBarState.update { it.copy(syncSummary = summary) }
                        }
                    }
                }
            }
        }
    }

    fun handleNotificationIntent(intent: android.content.Intent?) {
        val destination = intent?.getStringExtra(com.notifast.app.MainActivity.EXTRA_DESTINATION) ?: return
        when (destination) {
            com.notifast.app.MainActivity.DESTINATION_SCHEDULES -> {
                _pendingDeepLink.value = NotificationDeepLink.Schedules
            }
            com.notifast.app.MainActivity.DESTINATION_SUBJECT_DETAIL -> {
                val subjectName = intent.getStringExtra(com.notifast.app.MainActivity.EXTRA_SUBJECT_NAME) ?: ""
                val evaluationId = intent.getStringExtra(com.notifast.app.MainActivity.EXTRA_EVALUATION_ID)
                if (subjectName.isNotBlank()) {
                    _pendingDeepLink.value = NotificationDeepLink.SubjectDetail(subjectName, evaluationId)
                }
            }
            com.notifast.app.MainActivity.DESTINATION_SETTINGS -> {
                _pendingDeepLink.value = NotificationDeepLink.Settings
            }
        }
    }

    fun clearPendingDeepLink() {
        _pendingDeepLink.value = null
    }
}
