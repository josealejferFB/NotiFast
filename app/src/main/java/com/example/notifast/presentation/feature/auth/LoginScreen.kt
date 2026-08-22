package com.example.notifast.presentation.feature.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.notifast.R
import com.example.notifast.presentation.atoms.NotiFastFormField
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

import com.example.notifast.BuildConfig


@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    if (uiState.isLoggedIn) return

    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val gso = remember {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
        if (BuildConfig.WEB_CLIENT_ID.isNotBlank()) {
            builder.requestIdToken(BuildConfig.WEB_CLIENT_ID)
        }
        builder.build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { viewModel.onEvent(LoginEvent.GoogleSignIn(it)) }
        } catch (e: ApiException) {
            viewModel.onEvent(LoginEvent.GoogleError(e.localizedMessage ?: "Error al iniciar con Google"))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = stringResource(R.string.login_title),
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Ruby,
                letterSpacing = (-1).sp
            )

            Text(
                text = stringResource(R.string.login_subtitle),
                fontSize = 14.sp,
                color = Color.Gray.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isSignUp) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Aqua.copy(alpha = 0.3f), CircleShape)
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(32.dp), tint = Aqua)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    NotiFastFormField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = stringResource(R.string.login_field_firstname),
                        leadingIcon = Icons.Default.Person,
                        cursorColor = Aqua,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    NotiFastFormField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = stringResource(R.string.login_field_lastname),
                        leadingIcon = Icons.Default.Person,
                        cursorColor = Aqua,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            NotiFastFormField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.login_field_email),
                leadingIcon = Icons.Default.AlternateEmail,
                cursorColor = if (isSignUp) Aqua else Ruby
            )

            Spacer(modifier = Modifier.height(16.dp))

            NotiFastFormField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.login_field_password),
                leadingIcon = Icons.Default.LockOpen,
                cursorColor = if (isSignUp) Aqua else Ruby
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(color = if (isSignUp) Aqua else Ruby)
            } else {
                Button(
                    onClick = {
                        if (isSignUp) {
                            viewModel.onEvent(LoginEvent.SignUpWithDetails(email, password, firstName, lastName, selectedImageUri))
                        } else {
                            viewModel.onEvent(LoginEvent.LoginWithEmail(email, password))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSignUp) Aqua else Ruby),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
                ) {
                    Text(
                        text = if (isSignUp) stringResource(R.string.signup_button).uppercase() else stringResource(R.string.login_button).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text(stringResource(R.string.google_button))
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { isSignUp = !isSignUp }) {
                    Text(
                        text = if (isSignUp) stringResource(R.string.login_switch_to_login)
                               else stringResource(R.string.login_switch_to_signup),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            uiState.error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
