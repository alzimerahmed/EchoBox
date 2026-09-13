package com.alzimer.echobox.data.repository

import com.alzimer.echobox.data.cache.TtlCache
import com.alzimer.echobox.data.db.datasource.LocalDataSource
import com.alzimer.echobox.data.extension.getFullDataFromDB
import com.alzimer.echobox.data.parser.parseArtistData
import com.alzimer.echobox.domain.data.entities.ArtistEntity
import com.alzimer.echobox.domain.data.model.browse.artist.ArtistBrowse
import com.alzimer.echobox.domain.repository.ArtistRepository
import com.alzimer.echobox.domain.utils.Resource
import com.alzimer.echobox.kotlinytmusicscraper.YouTube
import com.alzimer.echobox.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import com.alzimer.echobox.domain.manager.DataStoreManager
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime

private const val ARTIST_BROWSE_TTL_MILLIS = 60L * 60 * 1000

internal class ArtistRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val youTube: YouTube,
    private val dataStoreManager: DataStoreManager,
) : ArtistRepository {
    // An artist page is static within a session — a fresh hit skips the network round-trip.
    private val artistBrowseCache = TtlCache<String, ArtistBrowse>(ARTIST_BROWSE_TTL_MILLIS)

    override fun getAllArtists(limit: Int): Flow<List<ArtistEntity>> =
        flow {
            emit(localDataSource.getAllArtists(limit))
        }.flowOn(Dispatchers.IO)

    override fun getArtistById(id: String): Flow<ArtistEntity?> =
        flow {
            emit(localDataSource.getArtist(id))
        }.flowOn(Dispatchers.IO)

    override suspend fun insertArtist(artistEntity: ArtistEntity) =
        withContext(Dispatchers.IO) {
            localDataSource.insertArtist(artistEntity)
        }

    override suspend fun updateArtistImage(
        channelId: String,
        thumbnail: String,
    ) = withContext(
        Dispatchers.Main,
    ) {
        localDataSource.updateArtistImage(
            channelId,
            thumbnail,
        )
    }

    override suspend fun updateArtistNameLogo(
        channelId: String,
        nameLogoUrl: String?,
        nameLogoColor: String?,
    ) = withContext(Dispatchers.IO) {
        localDataSource.updateArtistNameLogo(channelId, nameLogoUrl, nameLogoColor)
    }

    /**
     * Unfollowing also drops what only existed to serve the follow.
     *
     * Flipping the flag was all this ever did, so an artist's notifications and their new-releases
     * tracking row survived every unfollow — and once the unfollowed `artist` row is itself swept by
     * `SongRepository.clearHistoryAndOrphanedSongs`, they have nothing left to point back at.
     */
    override suspend fun updateFollowedStatus(
        channelId: String,
        followedStatus: Int,
    ): Boolean? =
        withContext(Dispatchers.Main) {
            localDataSource.updateFollowed(followedStatus, channelId)
            if (followedStatus == 0) {
                localDataSource.deleteNotificationsByChannelId(channelId)
                localDataSource.deleteFollowedArtistSingleAndAlbum(channelId)
            }
            // The local flag is already written above and stays written: Follow must not depend
            // on the network. Mirroring is opt-in, and its outcome is handed back rather than
            // swallowed so the caller can say something when the account could not be reached.
            if (dataStoreManager.syncFollowToYouTube.first() != DataStoreManager.TRUE) {
                return@withContext null
            }
            withContext(Dispatchers.IO) {
                setSubscription(channelId, followedStatus == 1)
            }
        }

    override fun syncFollowedArtistsToYouTube(): Flow<Pair<Int, Int>> =
        flow {
            val followed =
                getFullDataFromDB { limit, offset ->
                    localDataSource.getFollowedArtists(limit, offset)
                }
            // Sequential on purpose. These are writes to someone's account, and firing a few
            // hundred of them at once is exactly the shape that gets a session rate-limited.
            var done = 0
            followed.forEach { artist ->
                if (setSubscription(artist.channelId, true)) done++
            }
            emit(done to followed.size)
        }.flowOn(Dispatchers.IO)

    /** One subscribe/unsubscribe call, logged on failure. Returns whether the account was updated. */
    private suspend fun setSubscription(
        channelId: String,
        subscribe: Boolean,
    ): Boolean {
        val result =
            if (subscribe) {
                youTube.subscribeChannel(channelId)
            } else {
                youTube.unsubscribeChannel(channelId)
            }
        return result
            .onFailure {
                Logger.w("ArtistRepositoryImpl", "Channel subscription sync failed: ${it.message}")
            }.isSuccess
    }

    override fun getFollowedArtists(): Flow<List<ArtistEntity>> =
        flow {
            emit(
                getFullDataFromDB { limit, offset ->
                    localDataSource.getFollowedArtists(limit, offset)
                },
            )
        }.flowOn(Dispatchers.IO)

    override suspend fun updateArtistInLibrary(
        inLibrary: LocalDateTime,
        channelId: String,
    ) = withContext(Dispatchers.Main) {
        localDataSource.updateArtistInLibrary(
            inLibrary,
            channelId,
        )
    }

    override fun getArtistData(channelId: String): Flow<Resource<ArtistBrowse>> =
        flow {
            artistBrowseCache.get(channelId)?.let { cached ->
                emit(Resource.Success<ArtistBrowse>(cached))
                return@flow
            }
            runCatching {
                youTube
                    .artist(channelId)
                    .onSuccess { result ->
                        val parsed = parseArtistData(result)
                        artistBrowseCache.put(channelId, parsed)
                        emit(Resource.Success<ArtistBrowse>(parsed))
                    }.onFailure { e ->
                        Logger.d("Artist", "Error: ${e.message}")
                        emit(Resource.Error<ArtistBrowse>(e.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)
}