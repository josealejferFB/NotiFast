package com.notifast.app.presentation.feature.settings

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import com.notifast.app.presentation.theme.isDark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.AlertSeverity
import com.notifast.app.presentation.atoms.LocalNotiFastSnackbarHostState
import com.notifast.app.presentation.atoms.NotiFastSnackbarVisuals
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.cloud.CloudHubSheet
import com.notifast.app.presentation.feature.settings.components.SettingsDialogDispatcher
import com.notifast.app.presentation.feature.settings.components.SettingsSectionsContent

/**
 * NotiFast 3.0 Settings Orchestrator Screen (AAA Control Hub).
 * Ultra-lightweight composable conforming to strict Clean Architecture and SRP.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onSearchClick: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState = LocalNotiFastSnackbarHostState.current
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val lastScaleLabel = uiState.lastScaleLabel
    val lastConvertedCount = uiState.lastConvertedCount
    val gradeScaleConvertedSnack = stringResource(
        R.string.settings_grade_scale_converted_snack,
        lastConvertedCount,
        lastScaleLabel ?: ""
    )
    val gradeScaleChangedSnack = stringResource(
        R.string.settings_grade_scale_changed_snack,
        lastScaleLabel ?: ""
    )

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onEvent(SettingsEvent.AuthResult(result.data))
        } else {
            viewModel.onEvent(SettingsEvent.AuthResult(null))
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val shouldShowRationale = if (!isGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && context is Activity) {
            context.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            false
        }
        viewModel.onEvent(
            SettingsEvent.OnNotificationPermissionResult(
                isGranted = isGranted,
                shouldShowRationale = !isGranted && !shouldShowRationale
            )
        )
    }

    val requestNotificationPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.onEvent(SettingsEvent.RefreshNotificationPermission)
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(SettingsEvent.RefreshNotificationPermission)
    }

    LaunchedEffect(uiState.pendingAuthIntent) {
        uiState.pendingAuthIntent?.let { pi ->
            authLauncher.launch(IntentSenderRequest.Builder(pi.intentSender).build())
        }
    }

    LaunchedEffect(uiState.cloudError) {
        uiState.cloudError?.let { err ->
            snackbarHostState.showSnackbar(
                NotiFastSnackbarVisuals(
                    message = err,
                    severity = AlertSeverity.ERROR
                )
            )
            viewModel.onEvent(SettingsEvent.ClearCloudError)
        }
    }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) onLogout()
    }

    LaunchedEffect(uiState.snackbarMessage, uiState.error, lastScaleLabel, lastConvertedCount) {
        if (lastScaleLabel != null) {
            val message = if (lastConvertedCount > 0) gradeScaleConvertedSnack else gradeScaleChangedSnack
            snackbarHostState.showSnackbar(
                NotiFastSnackbarVisuals(
                    message = message,
                    severity = AlertSeverity.INFO
                )
            )
            viewModel.onEvent(SettingsEvent.ClearSnackbar)
        }
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(
                NotiFastSnackbarVisuals(
                    message = message,
                    severity = AlertSeverity.ERROR
                )
            )
            viewModel.onEvent(SettingsEvent.ClearSnackbar)
        }
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(
                NotiFastSnackbarVisuals(
                    message = message,
                    severity = AlertSeverity.SUCCESS
                )
            )
            viewModel.onEvent(SettingsEvent.ClearSnackbar)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NotiFastTopBar(
                    type = TopBarType.Home(
                        firstName = uiState.displayFirstName,
                        email = uiState.displayEmail,
                        profilePictureUrl = uiState.displayPhotoUrl,
                        contextText = stringResource(R.string.settings_title),
                        onProfileClick = onEditProfile,
                        onSearchClick = onSearchClick,
                        onCloudClick = { viewModel.onEvent(SettingsEvent.OpenCloudHub) },
                        syncSummary = uiState.syncSummary
                    )
                )
            }
        ) { padding ->
            SettingsSectionsContent(
                uiState = uiState,
                context = context,
                onEditProfile = onEditProfile,
                onEvent = viewModel::onEvent,
                onRequestPermission = requestNotificationPermission,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            )
        }
    }

    SettingsDialogDispatcher(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )

    if (uiState.isCloudHubOpen) {
        CloudHubSheet(
            onDismiss = { viewModel.onEvent(SettingsEvent.CloseCloudHub) }
        )
    }

    if (uiState.isCustomThemeSheetOpen) {
        com.notifast.app.presentation.feature.settings.components.CustomThemePaletteSheet(
            draftHex = uiState.customDraftColorHex,
            draftBackgroundHex = uiState.customDraftBackgroundHex,
            isDark = MaterialTheme.colorScheme.isDark,
            initialTab = uiState.customThemeSheetInitialTab,
            onColorSelected = { viewModel.onEvent(SettingsEvent.UpdateCustomDraftColor(it)) },
            onBackgroundHexSelected = { viewModel.onEvent(SettingsEvent.UpdateCustomDraftBackgroundHex(it)) },
            onApply = { viewModel.onEvent(SettingsEvent.ApplyCustomTheme) },
            onDismiss = { viewModel.onEvent(SettingsEvent.CloseCustomThemeSheet) }
        )
    }
}

