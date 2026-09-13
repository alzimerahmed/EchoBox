package com.alzimer.echobox.domain.repository

import com.alzimer.echobox.domain.data.entities.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * On-device audio discovered through the platform media index (MediaStore on Android).
 *
 * Local tracks are stored as `song` rows with `local_`-prefixed ids, so the rest of the app —
 * likes, local playlists, play counts, the queue — treats them like any other track. They are
 * never touched by the clear-history orphan sweep; only a rescan deletes them, and only when
 * the underlying file has disappeared.
 */
interface LocalMediaRepository {
    /** Local tracks in the library, ordered by when the file was added to the device. */
    val localSongs: Flow<List<SongEntity>>

    /** True when the platform media permission needed to enumerate audio is granted. */
    fun hasLocalAudioPermission(): Boolean

    /**
     * Full rescan: inserts newly seen files, prunes rows whose file disappeared, and stamps
     * [com.alzimer.echobox.domain.manager.DataStoreManager.setLocalFilesLastScan].
     *
     * Inserts are IGNORE-on-conflict, so a rescan never clobbers a row's liked flag, play time,
     * or playlist memberships.
     */
    suspend fun rescan(): LocalScanResult

    /** Drops every local track row — called when the feature is switched off. */
    suspend fun clearAll(): Int
}

data class LocalScanResult(
    val added: Int,
    val removed: Int,
    val total: Int,
)
