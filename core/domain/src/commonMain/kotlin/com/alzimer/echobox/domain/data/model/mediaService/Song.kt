package com.alzimer.echobox.domain.data.model.mediaService

import com.alzimer.echobox.domain.data.model.searchResult.songs.Album
import com.alzimer.echobox.domain.data.model.searchResult.songs.Artist
import com.alzimer.echobox.domain.data.model.searchResult.songs.Thumbnail

data class Song(
    val title: String?,
    val artists: List<Artist>?,
    val duration: Long,
    val lyrics: Any,
    val album: Album,
    val videoId: String,
    val thumbnail: Thumbnail?,
    val isLocal: Boolean,
)