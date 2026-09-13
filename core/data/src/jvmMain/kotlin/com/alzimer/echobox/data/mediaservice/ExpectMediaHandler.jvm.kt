package com.alzimer.echobox.data.mediaservice

import com.alzimer.echobox.domain.repository.AnalyticsRepository

actual fun createMediaServiceHandler(
    dataStoreManager: com.alzimer.echobox.domain.manager.DataStoreManager,
    songRepository: com.alzimer.echobox.domain.repository.SongRepository,
    streamRepository: com.alzimer.echobox.domain.repository.StreamRepository,
    localPlaylistRepository: com.alzimer.echobox.domain.repository.LocalPlaylistRepository,
    analyticsRepository: AnalyticsRepository,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
): com.alzimer.echobox.domain.mediaservice.handler.MediaPlayerHandler =
    JvmMediaPlayerHandlerImpl(
        dataStoreManager = dataStoreManager,
        songRepository = songRepository,
        streamRepository = streamRepository,
        localPlaylistRepository = localPlaylistRepository,
        analyticsRepository = analyticsRepository,
        coroutineScope = coroutineScope,
    )