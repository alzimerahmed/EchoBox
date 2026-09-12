package com.alzimer.echobox.domain.data.model.browse.artist

import com.alzimer.echobox.domain.data.model.searchResult.songs.Thumbnail
import com.alzimer.echobox.domain.data.type.HomeContentType

data class ResultPlaylist(
    val id: String,
    val author: String,
    val thumbnails: List<Thumbnail>,
    val title: String,
) : HomeContentType