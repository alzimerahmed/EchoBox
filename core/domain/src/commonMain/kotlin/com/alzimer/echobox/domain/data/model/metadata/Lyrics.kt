package com.alzimer.echobox.domain.data.model.metadata

import kotlinx.serialization.Serializable

@Serializable
data class Lyrics(
    val error: Boolean = false,
    val lines: List<Line>?,
    val syncType: String?,
    val simpMusicLyrics: EchoBoxLyrics? = null,
)

@Serializable
data class EchoBoxLyrics(
    val id: String,
    val vote: Int,
)