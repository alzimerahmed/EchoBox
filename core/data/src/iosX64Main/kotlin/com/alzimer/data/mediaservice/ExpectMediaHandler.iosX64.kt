package com.alzimer.echobox.data.mediaservice

actual fun createMediaServiceHandler(
    dataStoreManager: com.alzimer.echobox.domain.manager.DataStoreManager,
    songRepository: com.alzimer.echobox.domain.repository.SongRepository,
    streamRepository: com.alzimer.echobox.domain.repository.StreamRepository,
    localPlaylistRepository: com.alzimer.echobox.domain.repository.LocalPlaylistRepository,
    analyticsRepository: com.alzimer.echobox.domain.repository.AnalyticsRepository,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
): com.alzimer.echobox.domain.mediaservice.handler.MediaPlayerHandler {
    TODO("Not yet implemented")
}