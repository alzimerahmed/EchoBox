package com.alzimer.echobox.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alzimer.echobox.domain.data.entities.SongEntity
import com.alzimer.echobox.domain.utils.LocalResource
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import echobox.composeapp.generated.resources.Res
import echobox.composeapp.generated.resources.no_playlists_added

/**
 * Smoke tests for the library playlist grid: the empty state must show the empty text, and a
 * populated library must render without crashing. Full navigation is out of scope here — the
 * grid only needs a NavController for item clicks.
 */
@RunWith(AndroidJUnit4::class)
class GridLibraryPlaylistTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyLibraryShowsEmptyText() {
        composeRule.setContent {
            MaterialTheme {
                GridLibraryPlaylist<SongEntity>(
                    navController = rememberNavController(),
                    contentPadding = PaddingValues(0.dp),
                    data = LocalResource.Success(emptyList()),
                    emptyText = Res.string.no_playlists_added,
                    onReload = {},
                )
            }
        }
        composeRule.onNodeWithText("No playlists added").assertExists()
    }

    @Test
    fun loadingStateRenders() {
        composeRule.setContent {
            MaterialTheme {
                GridLibraryPlaylist<SongEntity>(
                    navController = rememberNavController(),
                    contentPadding = PaddingValues(0.dp),
                    data = LocalResource.Loading(),
                    emptyText = Res.string.no_playlists_added,
                    onReload = {},
                )
            }
        }
        composeRule.onRoot().assertExists()
    }
}
