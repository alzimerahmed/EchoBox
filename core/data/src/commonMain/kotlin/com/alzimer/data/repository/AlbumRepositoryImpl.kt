package com.alzimer.echobox.data.repository

import com.alzimer.echobox.data.cache.TtlCache
import com.alzimer.echobox.data.db.datasource.LocalDataSource
import com.alzimer.echobox.data.extension.getFullDataFromDB
import com.alzimer.echobox.data.mapping.toAlbumsResult
import com.alzimer.echobox.data.parser.parseAlbumData
import com.alzimer.echobox.domain.data.entities.AlbumEntity
import com.alzimer.echobox.domain.data.entities.FollowedArtistSingleAndAlbum
import com.alzimer.echobox.domain.data.model.browse.album.AlbumBrowse
import com.alzimer.echobox.domain.data.model.searchResult.albums.AlbumsResult
import com.alzimer.echobox.domain.repository.AlbumRepository
import com.alzimer.echobox.domain.utils.Resource
import com.alzimer.echobox.kotlinytmusicscraper.YouTube
import com.alzimer.echobox.kotlinytmusicscraper.models.AlbumItem
import com.alzimer.echobox.logger.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime

private const val TAG = "AlbumRepositoryImpl"
private const val ALBUM_BROWSE_TTL_MILLIS = 60L * 60 * 1000

internal class AlbumRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val youTube: YouTube,
) : AlbumRepository {
    // An album page's track list does not change — a fresh hit skips the network round-trip.
    private val albumBrowseCache = TtlCache<String, AlbumBrowse>(ALBUM_BROWSE_TTL_MILLIS)

    override fun getAllAlbums(limit: Int): Flow<List<AlbumEntity>> =
        flow {
            emit(localDataSource.getAllAlbums(limit))
        }.flowOn(Dispatchers.IO)

    override fun getAlbum(id: String): Flow<AlbumEntity?> =
        flow {
            emit(localDataSource.getAlbum(id))
        }.flowOn(Dispatchers.IO)

    override fun getAlbumAsFlow(id: String) = localDataSource.getAlbumAsFlow(id)

    override fun getLikedAlbums(): Flow<List<AlbumEntity>> =
        flow {
            emit(
                getFullDataFromDB { limit, offset ->
                    localDataSource.getLikedAlbums(
                        limit,
                        offset,
                    )
                },
            )
        }.flowOn(Dispatchers.IO)

    override fun insertAlbum(albumEntity: AlbumEntity) =
        flow {
            emit(localDataSource.insertAlbum(albumEntity))
        }.flowOn(Dispatchers.IO)

    override suspend fun updateAlbumLiked(
        albumId: String,
        likeStatus: Int,
    ) = withContext(Dispatchers.Main) { localDataSource.updateAlbumLiked(likeStatus, albumId) }

    override suspend fun updateAlbumInLibrary(
        inLibrary: LocalDateTime,
        albumId: String,
    ) = withContext(
        Dispatchers.Main,
    ) { localDataSource.updateAlbumInLibrary(inLibrary, albumId) }

    override suspend fun updateAlbumDownloadState(
        albumId: String,
        downloadState: Int,
    ) = withContext(Dispatchers.Main) {
        localDataSource.updateAlbumDownloadState(
            downloadState,
            albumId,
        )
    }

    override suspend fun insertFollowedArtistSingleAndAlbum(followedArtistSingleAndAlbum: FollowedArtistSingleAndAlbum) =
        withContext(Dispatchers.IO) {
            localDataSource.insertFollowedArtistSingleAndAlbum(followedArtistSingleAndAlbum)
        }

    override suspend fun deleteFollowedArtistSingleAndAlbum(channelId: String) =
        withContext(Dispatchers.IO) {
            localDataSource.deleteFollowedArtistSingleAndAlbum(channelId)
        }

    override suspend fun getAllFollowedArtistSingleAndAlbums(): Flow<List<FollowedArtistSingleAndAlbum>?> =
        flow {
            val list =
                getFullDataFromDB { limit, offset ->
                    localDataSource.getAllFollowedArtistSingleAndAlbums(limit, offset)
                }
            emit(list)
        }.flowOn(Dispatchers.IO)

    override suspend fun getFollowedArtistSingleAndAlbum(channelId: String): Flow<FollowedArtistSingleAndAlbum?> =
        flow {
            emit(localDataSource.getFollowedArtistSingleAndAlbum(channelId))
        }.flowOn(Dispatchers.IO)

    override fun getAlbumData(browseId: String): Flow<Resource<AlbumBrowse>> =
        flow {
            albumBrowseCache.get(browseId)?.let { cached ->
                emit(Resource.Success(cached))
                return@flow
            }
            try {
                youTube
                    .album(browseId, withSongs = true)
                    .onSuccess { result ->
                        val parsed = parseAlbumData(result)
                        albumBrowseCache.put(browseId, parsed)
                        emit(Resource.Success(parsed))
                    }.onFailure { e ->
                        Logger.d(TAG, "getAlbumData -> error: ${e.message ?: "unknown error"}")
                        emit(Resource.Error(e.message ?: "Unknown error while loading album"))
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.e(TAG, "getAlbumData -> error: ${e.message ?: "unknown error"}", e)
                emit(Resource.Error(e.message ?: "Unknown error while loading album"))
            }
        }.flowOn(Dispatchers.IO)

    override fun getAlbumMore(
        browseId: String,
        params: String,
    ): Flow<Pair<String, List<AlbumsResult>>?> =
        flow {
            try {
                youTube
                    .browse(browseId = browseId, params = params)
                    .onSuccess { data ->
                        Logger.w(TAG, "getAlbumMore -> result: $data")
                        val items =
                            (data.items.firstOrNull()?.items ?: emptyList()).mapNotNull { item ->
                                item as? AlbumItem
                            }
                        emit(
                            (data.title ?: "") to (
                                items.map {
                                    it.toAlbumsResult()
                                }
                            ),
                        )
                    }.onFailure {
                        Logger.d(TAG, "getAlbumMore -> error: ${it.message ?: "unknown error"}")
                        emit(null)
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.e(TAG, "getAlbumMore -> error: ${e.message ?: "unknown error"}", e)
                emit(null)
            }
        }.flowOn(Dispatchers.IO)
}