package com.alzimer.echobox.common

/** Supplied by the application entry point so build variants retain their real identity. */
data class AppIdentity(
    val applicationId: String,
    val versionName: String,
    val platform: String,
) {
    val userAgent: String
        get() = "EchoBox/$versionName ($applicationId; $platform)"
}
