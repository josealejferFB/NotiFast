package com.notifast.app.presentation.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.settings.components.SettingsDialogDispatcher
import com.notifast.app.presentation.feature.settings.components.SettingsSectionsContent

/**
 * NotiFast 3.0 Settings Orchestrator Screen (AAA Control Hub).
 * Ultra-lightweight composable (< 90 LOC) conforming to strict Clean Architecture and SRP.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
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

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) onLogout()
    }

    LaunchedEffect(uiState.snackbarMessage, uiState.error, lastScaleLabel, lastConvertedCount) {
        if (lastScaleLabel != null) {
            val message = if (lastConvertedCount > 0) gradeScaleConvertedSnack else gradeScaleChangedSnack
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(SettingsEvent.ClearSnackbar)
        }
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(SettingsEvent.ClearSnackbar)
        }
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(SettingsEvent.ClearSnackbar)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                NotiFastTopBar(
                    type = TopBarType.Home(
                        firstName = uiState.userProfile?.firstName ?: "",
                        email = uiState.userProfile?.email ?: "",
                        profilePictureUrl = uiState.userProfile?.profilePictureUrl,
                        contextText = stringResource(R.string.settings_title),
                        onProfileClick = onEditProfile
                    )
                )
            }
        ) { padding ->
            SettingsSectionsContent(
                uiState = uiState,
                context = context,
                onEditProfile = onEditProfile,
                onEvent = viewModel::onEvent,
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
}
