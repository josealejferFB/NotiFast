package com.example.notifast.presentation.atoms

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NotiFastDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Aceptar",
    dismissText: String = "Cancelar",
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    containerColor: Color = Color.White,
    confirmColor: Color? = null,
    confirmWeight: FontWeight? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = if (confirmColor != null) ButtonDefaults.textButtonColors(contentColor = confirmColor) else ButtonDefaults.textButtonColors()
            ) {
                Text(confirmText, fontWeight = confirmWeight)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissText) }
        },
        shape = shape,
        containerColor = containerColor
    )
}
