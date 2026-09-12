package com.alzimer.echobox.data.mediaservice

import com.alzimer.echobox.domain.manager.DataStoreManager
import com.alzimer.echobox.domain.mediaservice.handler.MediaPlayerHandler
import com.alzimer.echobox.domain.repository.AnalyticsRepository
import com.alzimer.echobox.domain.repository.LocalPlaylistRepository
import com.alzimer.echobox.domain.repository.SongRepository
import com.alzimer.echobox.domain.repository.StreamRepository
import kotlinx.coroutines.CoroutineScope

expect fun createMediaServiceHandler(
    dataStoreManager: DataStoreManager,
    songRepository: SongRepository,
    streamRepository: StreamRepository,
    localPlaylistRepository: LocalPlaylistRepository,
    analyticsRepository: AnalyticsRepository,
    coroutineScope: CoroutineScope,
): MediaPlayerHandler