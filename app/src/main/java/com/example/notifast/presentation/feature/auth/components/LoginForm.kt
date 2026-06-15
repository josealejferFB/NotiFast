package com.example.notifast.presentation.feature.auth.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.notifast.presentation.atoms.NotiFastTextField

@Composable
fun LoginForm(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    error: String?,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        NotiFastTextField(
            value = email,
            onValueChange = { onEmailChange(it); onClearError() },
            label = "Correo electrónico",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        NotiFastTextField(
            value = password,
            onValueChange = { onPasswordChange(it); onClearError() },
            label = "Contraseña",
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
