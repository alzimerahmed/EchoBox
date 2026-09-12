package com.alzimer.echobox.kotlinytmusicscraper.models.body

import com.alzimer.echobox.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetQueueBody(
    val context: Context,
    val videoIds: List<String>?,
    val playlistId: String?,
)