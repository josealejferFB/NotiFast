package com.notifast.app.presentation.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastFormField
import com.notifast.app.presentation.atoms.NotiFastImagePickerField
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.feature.onboarding.components.OnboardingHeader

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onOnboardingComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onOnboardingComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            OnboardingHeader()

            Spacer(modifier = Modifier.height(28.dp))

            NotiFastImagePickerField(
                existingImageUrl = uiState.profileImageUri?.toString(),
                placeholderText = stringResource(R.string.onboarding_avatar_hint),
                onImageSelected = { uri ->
                    viewModel.onEvent(OnboardingEvent.ImageSelected(uri))
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            NotiFastFormField(
                value = uiState.firstName,
                onValueChange = {
                    viewModel.onEvent(OnboardingEvent.FirstNameChanged(it))
                    if (uiState.error != null) {
                        viewModel.onEvent(OnboardingEvent.ClearError)
                    }
                },
                label = stringResource(R.string.onboarding_name_label),
                leadingIcon = Icons.Default.Person,
                isError = uiState.error != null && uiState.firstName.isBlank(),
                supportingText = if (uiState.error != null && uiState.firstName.isBlank()) {
                    {
                        Text(
                            text = stringResource(R.string.onboarding_error_name_required),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            NotiFastFormField(
                value = uiState.lastName,
                onValueChange = {
                    viewModel.onEvent(OnboardingEvent.LastNameChanged(it))
                },
                label = stringResource(R.string.onboarding_lastname_label),
                leadingIcon = Icons.Default.Person
            )

            if (uiState.error != null && uiState.firstName.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            NotiFastButton(
                text = stringResource(R.string.onboarding_start_button),
                onClick = {
                    viewModel.onEvent(OnboardingEvent.SubmitProfile)
                },
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
