@file:Suppress("ktlint:standard:filename")

package com.alzimer.echobox.expect.ui

import androidx.compose.runtime.Composable

interface LocalAudioPermissionRequester {
    /** Whether the audio-read permission needed to enumerate device media is currently granted. */
    fun isGranted(): Boolean

    /**
     * Reports through the requester's callback — immediately when already granted, or after the
     * system dialog closes. Answers exactly once per call.
     */
    fun requestIfNeeded()
}

/**
 * Gate in front of the MediaStore audio scan. Android 13+ asks for READ_MEDIA_AUDIO; older
 * releases read audio through the legacy READ_EXTERNAL_STORAGE grant instead.
 */
@Composable
expect fun rememberLocalAudioPermission(onResult: (granted: Boolean) -> Unit): LocalAudioPermissionRequester
