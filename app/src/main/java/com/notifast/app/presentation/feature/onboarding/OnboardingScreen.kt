package com.notifast.app.presentation.feature.onboarding

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.feature.onboarding.components.GoogleDriveBackupCard
import com.notifast.app.presentation.feature.onboarding.components.LocalProfileSection
import com.notifast.app.presentation.feature.onboarding.components.OnboardingHeader
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onOnboardingComplete: (autoOpenCloudHub: Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onEvent(OnboardingEvent.GoogleAuthResult(result.data))
        } else {
            viewModel.onEvent(OnboardingEvent.GoogleAuthResult(null))
        }
    }

    LaunchedEffect(uiState.pendingAuthIntent) {
        val pending = uiState.pendingAuthIntent
        if (pending != null) {
            authLauncher.launch(IntentSenderRequest.Builder(pending).build())
        }
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onOnboardingComplete(uiState.autoOpenCloudHub)
        }
    }

    val contentAlpha = remember { Animatable(0f) }
    val contentOffsetY = remember { Animatable(32f) }

    LaunchedEffect(Unit) {
        launch {
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        launch {
            contentOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground(showFloatingIcons = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .graphicsLayer {
                    alpha = contentAlpha.value
                    translationY = contentOffsetY.value
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            OnboardingHeader()

            Spacer(modifier = Modifier.height(12.dp))

            LocalProfileSection(
                firstName = uiState.firstName,
                lastName = uiState.lastName,
                profileImageUri = uiState.profileImageUri,
                isLoading = uiState.isLoading,
                isGoogleLoading = uiState.isGoogleLoading,
                error = uiState.error,
                onFirstNameChange = {
                    viewModel.onEvent(OnboardingEvent.FirstNameChanged(it))
                    if (uiState.error != null) {
                        viewModel.onEvent(OnboardingEvent.ClearError)
                    }
                },
                onLastNameChange = {
                    viewModel.onEvent(OnboardingEvent.LastNameChanged(it))
                },
                onImageSelected = { uri ->
                    viewModel.onEvent(OnboardingEvent.ImageSelected(uri))
                },
                onStartLocal = {
                    viewModel.onEvent(OnboardingEvent.SubmitProfile)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            GoogleDriveBackupCard(
                isGoogleLoading = uiState.isGoogleLoading,
                isLocalLoading = uiState.isLoading,
                onSignInWithGoogle = {
                    viewModel.onEvent(OnboardingEvent.SignInWithGoogle(context))
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
