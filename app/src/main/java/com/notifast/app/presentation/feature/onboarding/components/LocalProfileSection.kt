package com.notifast.app.presentation.feature.onboarding.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastFormField

/**
 * Compact and tactile local profile section for onboarding.
 * Features an integrated 76dp circular avatar picker, side-by-side name inputs,
 * and immediate local start action without scroll friction.
 */
@Composable
fun LocalProfileSection(
    firstName: String,
    lastName: String,
    profileImageUri: Uri?,
    isLoading: Boolean,
    isGoogleLoading: Boolean,
    error: String?,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onStartLocal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    NotiFastCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 18.dp,
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CompactAvatarPicker(
                imageUri = profileImageUri,
                onImageSelected = onImageSelected,
                enabled = !isLoading && !isGoogleLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NotiFastFormField(
                    value = firstName,
                    onValueChange = onFirstNameChange,
                    label = stringResource(R.string.onboarding_name_label),
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = error != null && firstName.isBlank()
                )

                NotiFastFormField(
                    value = lastName,
                    onValueChange = onLastNameChange,
                    label = stringResource(R.string.onboarding_lastname_label),
                    leadingIcon = Icons.Default.Badge,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onStartLocal()
                        }
                    )
                )
            }

            if (error != null) {
                com.notifast.app.presentation.atoms.NotiFastAlertBanner(
                    message = error,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            NotiFastButton(
                text = stringResource(R.string.onboarding_start_button),
                onClick = onStartLocal,
                isLoading = isLoading,
                enabled = !isLoading && !isGoogleLoading
            )
        }
    }
}
