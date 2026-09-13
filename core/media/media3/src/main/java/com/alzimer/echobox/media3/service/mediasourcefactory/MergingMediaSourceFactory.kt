package com.alzimer.echobox.media3.service.mediasourcefactory

import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import com.alzimer.echobox.common.LOCAL_FILE_ID_PREFIX
import com.alzimer.echobox.common.MERGING_DATA_TYPE
import com.alzimer.echobox.common.localFileContentUri
import com.alzimer.echobox.domain.manager.DataStoreManager
import com.alzimer.echobox.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@UnstableApi
internal class MergingMediaSourceFactory(
    private val defaultMediaSourceFactory: DefaultMediaSourceFactory,
    private val localMediaSourceFactory: MediaSource.Factory,
    private val dataStoreManager: DataStoreManager,
) : MediaSource.Factory {
    override fun setDrmSessionManagerProvider(drmSessionManagerProvider: DrmSessionManagerProvider): MediaSource.Factory {
        defaultMediaSourceFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
        localMediaSourceFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
        return this
    }

    override fun setLoadErrorHandlingPolicy(loadErrorHandlingPolicy: LoadErrorHandlingPolicy): MediaSource.Factory {
        defaultMediaSourceFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
        localMediaSourceFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
        return this
    }

    override fun getSupportedTypes(): IntArray = defaultMediaSourceFactory.supportedTypes

    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        if (mediaItem.mediaId.startsWith(LOCAL_FILE_ID_PREFIX)) {
            // On-device file: bypass the cache + stream-resolver stack entirely — its bytes are
            // already on disk and the raw mediaId carries no playable URI, so it is rewritten to
            // the MediaStore content URI first.
            return localMediaSourceFactory.createMediaSource(
                mediaItem
                    .buildUpon()
                    .setUri(localFileContentUri(mediaItem.mediaId))
                    .build(),
            )
        }
        Logger.w("Merging Media Source", mediaItem.mediaMetadata.description.toString())
        val getVideo = runBlocking(Dispatchers.IO) { dataStoreManager.watchVideoInsteadOfPlayingAudio.first() } == DataStoreManager.Values.TRUE
        Logger.w("Merging Media Source", getVideo.toString())
        if (mediaItem.mediaMetadata.description == MERGING_DATA_TYPE.VIDEO && getVideo) {
            val videoItem =
                mediaItem
                    .buildUpon()
                    .setMediaId("${MERGING_DATA_TYPE.VIDEO}${mediaItem.mediaId}")
                    .setCustomCacheKey("${MERGING_DATA_TYPE.VIDEO}${mediaItem.mediaId}")
                    .build()
            return MergingMediaSource(
                defaultMediaSourceFactory.createMediaSource(videoItem),
                defaultMediaSourceFactory.createMediaSource(mediaItem),
            )
        } else {
            return defaultMediaSourceFactory.createMediaSource(mediaItem)
        }

//        val default = defaultMediaSourceFactory.createMediaSource(mediaItem.buildUpon().setMediaId("AUDIO-${mediaItem.mediaId}").build())
    }
}