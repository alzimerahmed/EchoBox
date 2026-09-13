package com.alzimer.echobox.expect.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberLocalAudioPermission(onResult: (granted: Boolean) -> Unit): LocalAudioPermissionRequester {
    val context = LocalContext.current
    // Read through a State rather than capturing the lambda, for the same reason
    // rememberSaveImagePermission does: the caller passes a fresh lambda per recomposition.
    val currentOnResult by rememberUpdatedState(onResult)

    // READ_MEDIA_AUDIO exists only from API 33; below it MediaStore audio rides the legacy grant.
    val permission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            currentOnResult(granted)
        }

    return remember(context, launcher) {
        object : LocalAudioPermissionRequester {
            override fun isGranted(): Boolean =
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

            override fun requestIfNeeded() {
                if (isGranted()) {
                    currentOnResult(true)
                } else {
                    launcher.launch(permission)
                }
            }
        }
    }
}
