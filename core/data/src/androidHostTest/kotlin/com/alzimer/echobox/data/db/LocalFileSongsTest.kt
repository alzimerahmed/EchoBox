package com.alzimer.echobox.data.db

import app.cash.turbine.test
import com.alzimer.echobox.data.db.datasource.LocalDataSource
import com.alzimer.echobox.data.repository.LocalAudioRow
import com.alzimer.echobox.data.repository.toSongEntity
import com.alzimer.echobox.domain.data.entities.PairSongLocalPlaylist
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The local-file library reuses `song` under `local_` ids, so these tests pin down the two
 * contracts everything else leans on: the `local_` LIKE filter matches exactly that prefix
 * (underscore escaped — `localX` must NOT match), and the clear-history orphan sweep never
 * collects a local row. Only the scanner deletes local songs.
 */
@RunWith(AndroidJUnit4::class)
class LocalFileSongsTest {

    private lateinit var database: MusicDatabase
    private lateinit var dataSource: LocalDataSource

    @BeforeTest
    fun setUp() {
        database = buildInMemoryDatabase()
        dataSource = LocalDataSource(database.getDatabaseDao(), database)
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun getLocalFileSongsReturnsOnlyLocalRows() = runBlocking {
        dataSource.insertSong(testSong("v-remote"))
        dataSource.insertSong(testSong("local_11"))
        dataSource.insertSong(testSong("local_22"))

        database.getDatabaseDao().getLocalFileSongs().test {
            // inLibrary DESC can tie on a fast insert — assert membership, not order.
            assertEquals(
                setOf("local_11", "local_22"),
                awaitItem().map { it.videoId }.toSet(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun lookalikeIdsDoNotMatchTheLocalPrefix() = runBlocking {
        // "localX" would match an unescaped LIKE 'local_%'; "loca_1" misses the prefix entirely.
        // "LOCAL_" would match a case-insensitive LIKE — GLOB is case-sensitive, so it must not.
        dataSource.insertSong(testSong("localX9"))
        dataSource.insertSong(testSong("loca_9"))
        dataSource.insertSong(testSong("LOCAL_9"))
        dataSource.insertSong(testSong("local_9"))

        assertEquals(listOf("local_9"), dataSource.getLocalFileSongIds())
    }

    @Test
    fun deleteLocalFileSongsByIdsRefusesNonLocalIds() = runBlocking {
        dataSource.insertSong(testSong("local_5"))
        dataSource.insertSong(testSong("v-keep"))

        // Even if the caller mixes a remote id into the prune list, the LIKE guard drops it.
        val removed = dataSource.deleteLocalFileSongsByIds(listOf("local_5", "v-keep"))

        assertEquals(1, removed)
        assertNull(dataSource.getSong("local_5"))
        assertEquals("Song v-keep", dataSource.getSong("v-keep")?.title)
    }

    @Test
    fun deleteAllLocalFileSongsLeavesRemoteRows() = runBlocking {
        dataSource.insertSong(testSong("local_1"))
        dataSource.insertSong(testSong("local_2"))
        dataSource.insertSong(testSong("v-remote"))

        dataSource.deleteLocalFileSongsByIds(dataSource.getLocalFileSongIds())

        assertTrue(dataSource.getLocalFileSongIds().isEmpty())
        assertEquals("Song v-remote", dataSource.getSong("v-remote")?.title)
    }

    @Test
    fun orphanSweepSkipsLocalRows() = runBlocking {
        // Both rows are sweep-eligible — unliked, not downloaded, unreferenced. The local one
        // must be left alone: the file still exists, and the next scan would resurrect it anyway.
        dataSource.insertSong(testSong("local_1"))
        dataSource.insertSong(testSong("v-orphan"))

        val dao = database.getDatabaseDao()
        assertEquals(listOf("v-orphan"), dao.getOrphanedSongIds())

        dao.deleteSongsByIds(listOf("local_1", "v-orphan"))

        assertEquals("Song local_1", dataSource.getSong("local_1")?.title)
        assertNull(dataSource.getSong("v-orphan"))
    }

    @Test
    fun rekeyLocalSongIdKeepsLikesAndPlaylistMembership() = runBlocking {
        val dao = database.getDatabaseDao()
        // Simulates a MediaStore reindex: the same file comes back under a new _ID.
        dataSource.insertSong(testSong("local_1").copy(liked = true, totalPlayTime = 5_000))
        val playlistId = dao.insertLocalPlaylist(testPlaylist("mix"))
        dao.insertPairSongLocalPlaylist(
            PairSongLocalPlaylist(playlistId = playlistId, songId = "local_1", position = 0),
        )

        dataSource.rekeyLocalSongId("local_1", "local_9")

        val rekeyed = dataSource.getSong("local_9")
        assertNull(dataSource.getSong("local_1"))
        assertEquals(true, rekeyed?.liked)
        assertEquals(5_000, rekeyed?.totalPlayTime)
        assertEquals(
            "local_9",
            dao.getPlaylistPairSong(playlistId, limit = 10, offset = 0).single().songId,
        )
    }

    @Test
    fun rekeyLocalSongIdRefusesNonLocalIds() = runBlocking {
        val dao = database.getDatabaseDao()
        dataSource.insertSong(testSong("v-remote"))

        // The local_ GLOB guard on the UPDATE stops a remote row from ever being re-keyed.
        dataSource.rekeyLocalSongId("v-remote", "local_9")

        assertEquals("Song v-remote", dataSource.getSong("v-remote")?.title)
        assertNull(dataSource.getSong("local_9"))
    }

    @Test
    fun mapperMapsMediaStoreRowToSongEntity() = runBlocking {
        val entity =
            LocalAudioRow(
                mediaStoreId = 42L,
                title = "Blue in Green",
                artist = "Miles Davis",
                album = "Kind of Blue",
                albumId = 9L,
                durationMs = 327_000L,
                dateAddedSeconds = 1_700_000_000L,
                displayName = "01 Blue in Green.mp3",
            ).toSongEntity()

        assertEquals("local_42", entity.videoId)
        assertEquals("Blue in Green", entity.title)
        assertEquals(listOf("Miles Davis"), entity.artistName)
        // MediaStore ids are not YouTube browse ids — both stored null on purpose so no
        // consumer navigates to or stats-counts a numeric "channel".
        assertNull(entity.artistId)
        assertEquals("Kind of Blue", entity.albumName)
        assertEquals("5:27", entity.duration)
        assertEquals(327, entity.durationSeconds)
        assertEquals("content://media/external/audio/albumart/9", entity.thumbnails)
        assertNull(entity.albumId)
        assertEquals("Song", entity.videoType)
        assertEquals(true, entity.isAvailable)
    }

    @Test
    fun mapperSanitizesUnknownArtistAndFallsBackToDisplayName() = runBlocking {
        val entity =
            LocalAudioRow(
                mediaStoreId = 1L,
                title = null,
                artist = "<unknown>",
                album = null,
                albumId = null,
                durationMs = 0L,
                dateAddedSeconds = 0L,
                displayName = "field recording 01.wav",
            ).toSongEntity()

        assertEquals("field recording 01", entity.title)
        assertNull(entity.artistName)
        assertNull(entity.thumbnails)
    }
}
