package com.alzimer.echobox.kotlinytmusicscraper.models

import com.alzimer.echobox.kotlinytmusicscraper.models.subscriptionButton.SubscribeButtonRenderer
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionButton(
    val subscribeButtonRenderer: SubscribeButtonRenderer,
)