package com.notifast.app.presentation.feature.noteeditor.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.notifast.app.R
import java.io.File

class NoteImagePickerState(
    val isSourcePickerVisible: Boolean,
    val showSourcePicker: () -> Unit,
    val dismissSourcePicker: () -> Unit,
    val launchCamera: () -> Unit,
    val launchGallery: () -> Unit
)

@Composable
fun rememberNoteImagePicker(
    onImageSelected: (Uri?) -> Unit
): NoteImagePickerState {
    val context = LocalContext.current
    var isSourcePickerVisible by rememberSaveable { mutableStateOf(false) }
    var pendingCameraUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uriStr = pendingCameraUriString
        if (success && uriStr != null) {
            onImageSelected(Uri.parse(uriStr))
        } else {
            CameraUriHelper.cleanupEmptyFile(pendingCameraFile)
        }
        pendingCameraUriString = null
        pendingCameraFile = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri)
        }
    }

    val launchCameraDirectly: () -> Unit = {
        runCatching {
            val (file, uri) = CameraUriHelper.createTempPictureFile(context)
            pendingCameraFile = file
            pendingCameraUriString = uri.toString()
            takePictureLauncher.launch(uri)
        }.onFailure {
            pendingCameraFile = null
            pendingCameraUriString = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCameraDirectly()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.camera_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val requestCameraOrLaunch: () -> Unit = {
        isSourcePickerVisible = false
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCameraDirectly()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val launchGalleryAction: () -> Unit = {
        isSourcePickerVisible = false
        galleryLauncher.launch("image/*")
    }

    return remember(isSourcePickerVisible) {
        NoteImagePickerState(
            isSourcePickerVisible = isSourcePickerVisible,
            showSourcePicker = { isSourcePickerVisible = true },
            dismissSourcePicker = { isSourcePickerVisible = false },
            launchCamera = requestCameraOrLaunch,
            launchGallery = launchGalleryAction
        )
    }
}
