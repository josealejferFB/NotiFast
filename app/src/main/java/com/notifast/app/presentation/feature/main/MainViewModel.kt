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
import javax.inject.Inject

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

    val isDarkMode: StateFlow<Boolean?> = userPrefsRepository.isDarkModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val hapticsEnabled: StateFlow<Boolean> = userPrefsRepository.hapticsEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val themePresetId: StateFlow<String> = userPrefsRepository.themePresetIdFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "classic"
    )

    val backgroundPresetId: StateFlow<String> = userPrefsRepository.backgroundPresetIdFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "classic"
    )

    val customPrimaryHex: StateFlow<String?> = userPrefsRepository.customPrimaryHexFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val customBackgroundHex: StateFlow<String?> = userPrefsRepository.customBackgroundHexFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

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
