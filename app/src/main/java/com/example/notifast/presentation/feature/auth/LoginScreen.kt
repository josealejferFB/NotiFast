package com.example.notifast.presentation.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifast.presentation.atoms.NotiFastBackground
import com.example.notifast.presentation.atoms.NotiFastButton
import com.example.notifast.presentation.atoms.NotiFastCircularLoading
import com.example.notifast.presentation.feature.auth.components.LoginForm

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoggedIn) {
        onLoginSuccess()
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastBackground()
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("NotiFast", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(32.dp))
            LoginForm(
                email = uiState.email,
                password = uiState.password,
                onEmailChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
                onPasswordChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
                error = uiState.error,
                onClearError = { viewModel.onEvent(LoginEvent.ClearError) }
            )
            Spacer(Modifier.height(16.dp))
            if (uiState.isLoading) {
                NotiFastCircularLoading()
            } else {
                NotiFastButton(text = "Iniciar Sesión", onClick = { viewModel.onEvent(LoginEvent.LoginClicked) })
                Spacer(Modifier.height(8.dp))
                NotiFastButton(text = "Registrarse", onClick = { viewModel.onEvent(LoginEvent.SignUpClicked) })
            }
        }
    }
}
