package com.alzimer.echobox.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alzimer.echobox.domain.data.entities.LocalPlaylistEntity
import com.alzimer.echobox.domain.data.entities.SongEntity

/**
 * Builds a fresh in-memory [MusicDatabase] for host tests. Robolectric supplies the Context the
 * Android Room builder requires; the database itself never touches disk.
 */
internal fun buildInMemoryDatabase(): MusicDatabase {
    val context = ApplicationProvider.getApplicationContext<Context>()
    return Room
        .inMemoryDatabaseBuilder(context, MusicDatabase::class.java)
        .addTypeConverter(Converters())
        .allowMainThreadQueries()
        // Robolectric's native SQLite can serve reads from a second pooled connection before a
        // just-committed write on the first is visible. Pinning query + transaction work to one
        // direct executor serializes every statement and removes the race.
        .setQueryExecutor { it.run() }
        .setTransactionExecutor { it.run() }
        .build()
}

internal fun testSong(
    videoId: String,
    title: String = "Song $videoId",
): SongEntity =
    SongEntity(
        videoId = videoId,
        albumId = "album-$videoId",
        albumName = "Album $videoId",
        artistId = listOf("artist-1", "artist-2"),
        artistName = listOf("Artist One", "Artist Two"),
        duration = "3:00",
        durationSeconds = 180,
        isAvailable = true,
        isExplicit = false,
        likeStatus = "LIKE",
        title = title,
        videoType = "MUSIC_VIDEO_TYPE_ATV",
        category = null,
        resultType = "song",
    )

internal fun testPlaylist(title: String = "Test playlist"): LocalPlaylistEntity =
    LocalPlaylistEntity(
        title = title,
        tracks = emptyList(),
    )
