package com.alzimer.echobox.data.db

import DatabaseDao

import com.alzimer.echobox.domain.data.entities.PairSongLocalPlaylist
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for the local-playlist persistence path: the transaction that creates a
 * playlist and its pair rows, position ordering, and cascade delete. This is the highest-risk
 * persistence logic in the app — the pair table carries a foreign key to `song.videoId` and an
 * `on_delete_pair_song_local_playlist` trigger renumbers positions.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseDaoPlaylistTest {

    private lateinit var database: MusicDatabase
    private lateinit var dao: DatabaseDao

    @BeforeTest
    fun setUp() {
        database = buildInMemoryDatabase()
        dao = database.getDatabaseDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertLocalPlaylistWithTracksCreatesOrderedPairRows() = runBlocking {
        listOf("v1", "v2", "v3").forEach { dao.insertSong(testSong(it)) }

        val playlistId = dao.insertLocalPlaylistWithTracks(testPlaylist(), listOf("v1", "v2", "v3"))

        assertNotEquals(-1L, playlistId)
        val pairs = dao.getPlaylistPairSong(playlistId, limit = 10, offset = 0)
        assertEquals(listOf("v1", "v2", "v3"), pairs.map { it.songId })
        assertEquals(listOf(0, 1, 2), pairs.map { it.position })
    }

    @Test
    fun insertLocalPlaylistWithTracksRejectsUnknownVideoIds() = runBlocking {
        // No song rows inserted: the pair table's foreign key makes the whole transaction throw,
        // so no playlist row is written either.
        assertFailsWith<android.database.sqlite.SQLiteConstraintException> {
            dao.insertLocalPlaylistWithTracks(testPlaylist(), listOf("ghost"))
        }
        assertTrue(dao.getAllLocalPlaylists(limit = 10, offset = 0).isEmpty())
    }

    @Test
    fun deleteLocalPlaylistCascadesToPairRows() = runBlocking {
        listOf("v1", "v2").forEach { dao.insertSong(testSong(it)) }
        val playlistId = dao.insertLocalPlaylistWithTracks(testPlaylist(), listOf("v1", "v2"))

        dao.deleteLocalPlaylist(playlistId)

        assertNull(dao.getLocalPlaylist(playlistId))
        assertTrue(dao.getPlaylistPairSong(playlistId, limit = 10, offset = 0).isEmpty())
    }

    @Test
    fun updateLocalPlaylistTitlePersists() = runBlocking {
        val playlistId = dao.insertLocalPlaylistWithTracks(testPlaylist(), emptyList())

        dao.updateLocalPlaylistTitle("Renamed", playlistId)

        assertEquals("Renamed", dao.getLocalPlaylist(playlistId)?.title)
    }

    @Test
    fun getPlaylistPairOfSongFindsRowByVideoId() = runBlocking {
        dao.insertSong(testSong("v1"))
        val playlistId = dao.insertLocalPlaylistWithTracks(testPlaylist(), listOf("v1"))

        val pair = dao.getPlaylistPairOfSong("v1", playlistId)

        assertEquals("v1", pair.songId)
        assertEquals(playlistId, pair.playlistId)
        assertEquals(0, pair.position)
    }

    @Test
    fun duplicatePlaylistTitleIsAllowed() = runBlocking {
        dao.insertSong(testSong("v1"))
        val first = dao.insertLocalPlaylistWithTracks(testPlaylist("Same"), listOf("v1"))
        val second = dao.insertLocalPlaylistWithTracks(testPlaylist("Same"), listOf("v1"))

        val all = dao.getAllLocalPlaylists(limit = 10, offset = 0)
        assertEquals(2, all.size)
        assertEquals(first, all.minOf { it.id })
        assertNotEquals(-1L, second)
        // One pair row per inserted track — `second` carries a single track.
        assertEquals(1, dao.getPlaylistPairSong(second, limit = 10, offset = 0).size)
    }

    @Test
    fun pairRowReplaceStrategyUpdatesPositionWithoutDuplicating() = runBlocking {
        dao.insertSong(testSong("v1"))
        val playlistId = dao.insertLocalPlaylistWithTracks(testPlaylist(), listOf("v1"))
        val original = dao.getPlaylistPairOfSong("v1", playlistId)

        dao.insertPairSongLocalPlaylist(
            PairSongLocalPlaylist(
                id = original.id,
                playlistId = playlistId,
                songId = "v1",
                position = 5,
            ),
        )

        val pairs = dao.getPlaylistPairSong(playlistId, limit = 10, offset = 0)
        assertEquals(1, pairs.size)
        assertEquals(5, pairs.first().position)
    }
}
