package com.alzimer.echobox.domain.data.model.mood.genre

import com.alzimer.echobox.domain.data.model.searchResult.songs.Thumbnail
import com.alzimer.echobox.domain.data.type.HomeContentType

data class Content(
    val playlistBrowseId: String,
    val thumbnail: List<Thumbnail>?,
    val title: Title,
) : HomeContentType