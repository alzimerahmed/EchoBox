package com.alzimer.echobox.kotlinytmusicscraper.models.body

import com.alzimer.echobox.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)