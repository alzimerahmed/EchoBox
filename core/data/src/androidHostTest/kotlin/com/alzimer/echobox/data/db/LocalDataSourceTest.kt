package com.alzimer.echobox.data.db

import DatabaseDao

import app.cash.turbine.test
import com.alzimer.echobox.data.db.datasource.LocalDataSource
import com.alzimer.echobox.domain.data.entities.SearchHistory
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests over [LocalDataSource], the data-layer seam every repository reads the
 * database through. Covers the `List<String>` JSON type converters (artistId/artistName columns),
 * the reactive song flow, and search history.
 */
@RunWith(AndroidJUnit4::class)
class LocalDataSourceTest {

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
    fun insertSongRoundTripsListConverters() = runBlocking {
        dataSource.insertSong(testSong("abc"))

        val song = dataSource.getSong("abc")

        assertEquals(listOf("artist-1", "artist-2"), song?.artistId)
        assertEquals(listOf("Artist One", "Artist Two"), song?.artistName)
        assertEquals("Song abc", song?.title)
        assertEquals(180, song?.durationSeconds)
    }

    @Test
    fun getSongAsFlowEmitsAfterInsert() = runBlocking {
        val dao = database.getDatabaseDao()
        dao.getSongAsFlow("abc").test {
            assertNull(awaitItem())

            dao.insertSong(testSong("abc"))

            assertEquals("Song abc", awaitItem()?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllSongsRespectsLimit() = runBlocking {
        // inLibrary is stored at millisecond precision — a fast insert loop can tie, and SQLite
        // defines no order within a tie. Give each row a distinct timestamp.
        (1..5).forEach {
            dataSource.insertSong(testSong("v$it").copy(inLibrary = LocalDateTime(2026, 1, 1, 0, it, 0)))
        }

        val songs = dataSource.getAllSongs(limit = 3)

        // Ordered by inLibrary DESC — the most recently inserted rows come first.
        assertEquals(listOf("v5", "v4", "v3"), songs.map { it.videoId })

        val all = dataSource.getAllSongs(limit = 10)
        assertEquals(5, all.size)
    }

    @Test
    fun searchHistoryInsertFetchDelete() = runBlocking {
        dataSource.insertSearchHistory(SearchHistory("lofi beats"))
        dataSource.insertSearchHistory(SearchHistory("jazz"))

        assertEquals(listOf("lofi beats", "jazz"), dataSource.getSearchHistory().map { it.query })

        dataSource.deleteSearchHistory()

        assertTrue(dataSource.getSearchHistory().isEmpty())
    }
}
