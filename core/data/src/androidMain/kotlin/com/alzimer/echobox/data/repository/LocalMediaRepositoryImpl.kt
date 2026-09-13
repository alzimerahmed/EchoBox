package com.alzimer.echobox.data.repository

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.alzimer.echobox.common.LOCAL_FILE_ID_PREFIX
import com.alzimer.echobox.common.localFileContentUri
import com.alzimer.echobox.data.db.datasource.LocalDataSource
import com.alzimer.echobox.domain.data.entities.SongEntity
import com.alzimer.echobox.domain.manager.DataStoreManager
import com.alzimer.echobox.domain.repository.LocalMediaRepository
import com.alzimer.echobox.domain.repository.LocalScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.IOException
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * One MediaStore.Audio row, flattened to plain values so [toSongEntity] is host-testable
 * without a device or a live ContentResolver.
 */
internal data class LocalAudioRow(
    val mediaStoreId: Long,
    val title: String?,
    val artist: String?,
    val album: String?,
    val albumId: Long?,
    val durationMs: Long,
    val dateAddedSeconds: Long,
    val displayName: String?,
)

internal fun LocalAudioRow.toSongEntity(): SongEntity {
    // MediaStore reports the literal "<unknown>" for untagged files.
    val cleanArtist = artist?.takeIf { it.isNotBlank() && it != "<unknown>" }
    val fallbackTitle = displayName?.substringBeforeLast('.', displayName)
    val seconds = (durationMs / 1000L).toInt()
    return SongEntity(
        videoId = LOCAL_FILE_ID_PREFIX + mediaStoreId,
        // MediaStore ids are not YouTube browse ids, so NEITHER goes into albumId/artistId:
        // anything navigating on them (album screen, artist sheet, Wrapped's event_artist
        // channelIds) would resolve a numeric id as a channel and dead-end.
        albumId = null,
        albumName = album?.takeIf { it.isNotBlank() },
        artistId = null,
        artistName = cleanArtist?.let { listOf(it) },
        duration = "%d:%02d".format(seconds / 60, seconds % 60),
        durationSeconds = seconds,
        isAvailable = true,
        isExplicit = false,
        likeStatus = "INDIFFERENT",
        thumbnails = albumId?.let { "content://media/external/audio/albumart/$it" },
        title = title?.takeIf { it.isNotBlank() } ?: fallbackTitle?.takeIf { it.isNotBlank() } ?: "Unknown",
        videoType = "Song",
        category = null,
        resultType = null,
        favoriteAt = null,
        downloadedAt = null,
        inLibrary =
            if (dateAddedSeconds > 0) {
                Instant
                    .fromEpochSeconds(dateAddedSeconds)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            } else {
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            },
    )
}

internal fun queryLocalAudio(contentResolver: ContentResolver): List<LocalAudioRow> {
    val projection =
        arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DISPLAY_NAME,
        )
    val rows = mutableListOf<LocalAudioRow>()
    val cursor =
        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC",
        ) ?: throw IOException("MediaStore returned null cursor")
    // A null cursor is NOT an empty library — it is a dead provider. Throwing routes it into
    // the caller's abort path, where a truncated scan must not reach the prune step.
    cursor.use {
        val colId = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val colTitle = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val colArtist = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val colAlbum = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val colAlbumId = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val colDuration = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val colDateAdded = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
        val colDisplayName = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        while (it.moveToNext()) {
            rows +=
                LocalAudioRow(
                    mediaStoreId = it.getLong(colId),
                    title = it.getString(colTitle),
                    artist = it.getString(colArtist),
                    album = it.getString(colAlbum),
                    albumId = it.getLong(colAlbumId).takeIf { id -> id != 0L },
                    durationMs = it.getLong(colDuration),
                    dateAddedSeconds = it.getLong(colDateAdded),
                    displayName = it.getString(colDisplayName),
                )
        }
    }
    return rows
}

internal class LocalMediaRepositoryImpl(
    private val context: Context,
    private val localDataSource: LocalDataSource,
    private val dataStoreManager: DataStoreManager,
) : LocalMediaRepository {
    private val scanMutex = Mutex()

    override val localSongs: Flow<List<SongEntity>> = localDataSource.getLocalFileSongs()

    override fun hasLocalAudioPermission(): Boolean =
        context.checkSelfPermission(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            },
        ) == PackageManager.PERMISSION_GRANTED

    override suspend fun rescan(): LocalScanResult =
        scanMutex.withLock {
            withContext(Dispatchers.IO) {
                if (!hasLocalAudioPermission()) return@withContext LocalScanResult(0, 0, 0)

                // A failed query must yield no rows at all: pruning below treats "not in the scan"
                // as "file deleted", so a partial result would wrongly delete real library entries.
                val rows =
                    runCatching { queryLocalAudio(context.contentResolver) }
                        .getOrElse { return@withContext LocalScanResult(0, 0, 0) }

                val before = localDataSource.getLocalFileSongIds().toSet()
                localDataSource.insertSongs(rows.map { it.toSongEntity() })

                val seenIds = rows.mapTo(HashSet(rows.size)) { LOCAL_FILE_ID_PREFIX + it.mediaStoreId }

                // An empty scan over a populated index is a mounted-volume failure, not the user
                // deleting every file. Only explicit clearAll() is allowed to empty the table.
                if (seenIds.isEmpty() && before.isNotEmpty()) {
                    return@withContext LocalScanResult(0, 0, before.size)
                }

                val stale = (localDataSource.getLocalFileSongIds() - seenIds)

                // MediaStore re-keys _ID when it rebuilds its index, so a "new" row can be a known
                // file under a new id. Match stale rows to fresh rows by (title, artist, duration)
                // and re-key instead of delete+insert — likes, play time and playlist membership
                // follow the file rather than dying with the old id.
                val candidates = rows.filter { LOCAL_FILE_ID_PREFIX + it.mediaStoreId !in before }.toMutableList()
                val goneIds = mutableListOf<String>()
                var rekeyed = 0
                for (oldId in stale) {
                    val entity = localDataSource.getSong(oldId)
                    val match = entity?.let { matchScanRow(it, candidates) }
                    if (match != null) {
                        candidates.remove(match)
                        localDataSource.rekeyLocalSongId(oldId, LOCAL_FILE_ID_PREFIX + match.mediaStoreId)
                        rekeyed++
                    } else {
                        goneIds += oldId
                    }
                }

                // A scan can silently truncate (a volume unmounted mid-list) and report live files
                // as missing — verify each survivor against MediaStore before it is deleted.
                val deadIds = goneIds.filter { !mediaStoreHasRow(it) }
                val removed = localDataSource.deleteLocalFileSongsByIds(deadIds)

                dataStoreManager.setLocalFilesLastScan(Clock.System.now().toEpochMilliseconds())
                LocalScanResult(
                    added = seenIds.count { it !in before } - rekeyed,
                    removed = removed,
                    total = rows.size,
                )
            }
        }

    override suspend fun clearAll(): Int =
        withContext(Dispatchers.IO) {
            localDataSource.deleteLocalFileSongsByIds(localDataSource.getLocalFileSongIds())
        }

    /** Best-effort identity match used to survive a MediaStore reindex; a unique hit only. */
    private fun matchScanRow(
        entity: SongEntity,
        candidates: List<LocalAudioRow>,
    ): LocalAudioRow? {
        val storedArtist = entity.artistName?.firstOrNull()
        return candidates
            .filter { row ->
                row.title?.takeIf { it.isNotBlank() } == entity.title &&
                    (row.artist?.takeIf { it.isNotBlank() && it != "<unknown>" }) == storedArtist &&
                    abs(row.durationMs / 1000L - entity.durationSeconds) <= 1L
            }.singleOrNull()
    }

    private fun mediaStoreHasRow(videoId: String): Boolean =
        runCatching {
            context.contentResolver
                .query(
                    Uri.parse(localFileContentUri(videoId)),
                    arrayOf(MediaStore.Audio.Media._ID),
                    null,
                    null,
                    null,
                )?.use { it.moveToFirst() } == true
        }.getOrDefault(false)
}
