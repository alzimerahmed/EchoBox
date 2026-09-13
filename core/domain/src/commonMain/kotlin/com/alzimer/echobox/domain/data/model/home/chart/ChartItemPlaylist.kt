package com.alzimer.echobox.domain.data.model.home.chart

import com.alzimer.echobox.domain.data.model.browse.artist.ResultPlaylist

data class ChartItemPlaylist(
    val title: String,
    val playlists: List<ResultPlaylist>,
)