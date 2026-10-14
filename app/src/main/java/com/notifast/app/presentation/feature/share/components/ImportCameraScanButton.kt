package com.notifast.app.presentation.feature.share.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R

/**
 * Camera QR scanner trigger button for import bottom sheet.
 */
@Composable
fun ImportCameraScanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.import_scan_camera_btn),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
