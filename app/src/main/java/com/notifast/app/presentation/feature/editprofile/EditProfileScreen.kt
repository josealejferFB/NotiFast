package com.notifast.app.presentation.feature.editprofile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.notifast.app.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                com.notifast.app.presentation.atoms.NotiFastTopBar(
                    type = com.notifast.app.presentation.atoms.TopBarType.Detail(
                        title = stringResource(R.string.edit_profile_title),
                        onBackClick = onBack
                    )
                )
            }
        ) { padding ->
        if (uiState.isLoading || uiState.userProfile == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Ruby)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else if (uiState.userProfile?.profilePictureUrl != null) {
                        AsyncImage(model = uiState.userProfile?.profilePictureUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedTextField(
                    value = uiState.firstName,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.UpdateFirstName(it)) },
                    label = { Text(stringResource(R.string.edit_profile_first_name_label)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Aqua,
                        focusedLabelColor = Aqua,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.lastName,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.UpdateLastName(it)) },
                    label = { Text(stringResource(R.string.edit_profile_last_name_label)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Aqua,
                        focusedLabelColor = Aqua,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { viewModel.onEvent(EditProfileEvent.SaveProfile(selectedImageUri)) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ruby),
                    enabled = uiState.firstName.isNotBlank() && !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.edit_profile_btn_update), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
}
