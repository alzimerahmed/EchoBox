package com.alzimer.echobox.kotlinytmusicscraper.pages

import com.alzimer.echobox.kotlinytmusicscraper.models.AlbumItem
import com.alzimer.echobox.kotlinytmusicscraper.models.VideoItem

data class ExplorePage(
    val released: List<AlbumItem>,
    val musicVideo: List<VideoItem>,
)