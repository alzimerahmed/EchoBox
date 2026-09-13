package com.alzimer.echobox.expect.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/** Dead source set — no desktop target builds. Kept so the expect/actual pair stays complete. */
@Composable
actual fun rememberLocalAudioPermission(onResult: (granted: Boolean) -> Unit): LocalAudioPermissionRequester {
    val currentOnResult by rememberUpdatedState(onResult)
    return remember {
        object : LocalAudioPermissionRequester {
            override fun isGranted(): Boolean = false

            override fun requestIfNeeded() = currentOnResult(false)
        }
    }
}
