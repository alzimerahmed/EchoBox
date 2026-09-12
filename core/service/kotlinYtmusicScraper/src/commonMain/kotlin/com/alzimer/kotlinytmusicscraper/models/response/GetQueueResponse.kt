package com.alzimer.echobox.kotlinytmusicscraper.models.response

import com.alzimer.echobox.kotlinytmusicscraper.models.PlaylistPanelRenderer
import kotlinx.serialization.Serializable

@Serializable
data class GetQueueResponse(
    val queueDatas: List<QueueData>,
) {
    @Serializable
    data class QueueData(
        val content: PlaylistPanelRenderer.Content,
    )
}