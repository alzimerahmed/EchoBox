package com.alzimer.echobox.kotlinytmusicscraper.pages

import com.alzimer.echobox.kotlinytmusicscraper.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)