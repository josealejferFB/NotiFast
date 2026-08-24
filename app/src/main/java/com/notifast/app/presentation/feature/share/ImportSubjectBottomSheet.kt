package com.notifast.app.presentation.feature.share

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.notifast.app.R
import com.notifast.app.data.model.package_data.SubjectPackage
import com.notifast.app.data.model.package_data.SubjectPackageCodec
import com.notifast.app.domain.model.package_data.ConflictStrategy
import com.notifast.app.domain.usecase.ImportSubjectPackageUseCase
import com.notifast.app.presentation.feature.share.components.SubjectConflictDialog
import com.notifast.app.presentation.theme.ThemeUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSubjectBottomSheet(
    userId: String,
    importUseCase: ImportSubjectPackageUseCase,
    onDismiss: () -> Unit,
    onSubjectImported: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var payloadInput by remember { mutableStateOf("") }
    var parsedPackage by remember { mutableStateOf<SubjectPackage?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showConflictDialog by remember { mutableStateOf(false) }

    val scanner = remember(context) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
        GmsBarcodeScanning.getClient(context, options)
    }

    fun parseInput(input: String) {
        payloadInput = input
        errorText = null
        val trimmed = input.trim()
        if (trimmed.length > 20) {
            try {
                parsedPackage = SubjectPackageCodec.decodeFromCompressedString(trimmed)
            } catch (_: Exception) {
                parsedPackage = null
            }
        } else {
            parsedPackage = null
        }
    }

    fun launchCameraScanner() {
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                barcode.rawValue?.let { scannedCode ->
                    parseInput(scannedCode)
                }
            }
            .addOnCanceledListener {
                // User dismissed or canceled scanning
            }
            .addOnFailureListener { e ->
                val isCancelled = e is com.google.mlkit.common.MlKitException &&
                        e.errorCode == com.google.mlkit.common.MlKitException.CODE_SCANNER_CANCELLED
                if (!isCancelled) {
                    Toast.makeText(context, context.getString(R.string.import_scanner_error), Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun executeImport(strategy: ConflictStrategy = ConflictStrategy.RENAME) {
        if (userId.isBlank() || payloadInput.isBlank()) return
        isImporting = true
        errorText = null
        scope.launch {
            val result = importUseCase(userId, payloadInput.trim(), strategy)
            isImporting = false
            result.onSuccess { importedName ->
                Toast.makeText(context, context.getString(R.string.import_success_toast, importedName), Toast.LENGTH_SHORT).show()
                onSubjectImported(importedName)
                onDismiss()
            }.onFailure { err ->
                if (err is IllegalStateException && err.message?.contains("ya existe") == true) {
                    showConflictDialog = true
                } else {
                    errorText = err.message ?: context.getString(R.string.import_invalid_code)
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.import_subject_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.import_subject_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Primary Action: Camera QR Scan Button
            Button(
                onClick = { launchCameraScanner() },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
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

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Text(
                    text = stringResource(R.string.import_scan_or_paste),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = payloadInput,
                onValueChange = { parseInput(it) },
                label = { Text(stringResource(R.string.import_paste_hint)) },
                placeholder = { Text("NOTIFAST1:eJy1V8...") },
                singleLine = false,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                isError = errorText != null,
                supportingText = errorText?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Preview Card if valid
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            AnimatedVisibility(visible = parsedPackage != null) {
                val pkg = parsedPackage
                if (pkg != null) {
                    val subjectColor = ThemeUtils.getAccessibleColor(pkg.subject.colorHex, isDark)
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(subjectColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pkg.subject.name.take(2).uppercase(),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = subjectColor
                                    )
                                }

                                Column {
                                    Text(
                                        text = pkg.subject.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${pkg.schedules.size} horarios • ${pkg.evaluations.size} evaluaciones • ${pkg.notes.size} apuntes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { executeImport() },
                enabled = parsedPackage != null && !isImporting,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(androidx.compose.material.icons.Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.import_btn_action), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showConflictDialog && parsedPackage != null) {
        SubjectConflictDialog(
            subjectName = parsedPackage!!.subject.name,
            onSelectStrategy = { strategy ->
                showConflictDialog = false
                executeImport(strategy)
            },
            onDismiss = { showConflictDialog = false }
        )
    }
}
