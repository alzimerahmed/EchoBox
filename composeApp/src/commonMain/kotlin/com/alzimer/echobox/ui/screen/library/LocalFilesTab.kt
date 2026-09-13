package com.alzimer.echobox.ui.screen.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alzimer.echobox.common.Config
import com.alzimer.echobox.common.LOCAL_FILES_SOURCE_ID
import com.alzimer.echobox.domain.data.entities.SongEntity
import com.alzimer.echobox.domain.mediaservice.handler.PlaylistType
import com.alzimer.echobox.domain.mediaservice.handler.QueueData
import com.alzimer.echobox.domain.utils.toTrack
import com.alzimer.echobox.expect.ui.rememberLocalAudioPermission
import com.alzimer.echobox.extension.isScrollingUp
import com.alzimer.echobox.ui.component.EndOfPage
import com.alzimer.echobox.ui.component.NowPlayingBottomSheet
import com.alzimer.echobox.ui.component.RippleIconButton
import com.alzimer.echobox.ui.component.SongFullWidthItems
import com.alzimer.echobox.ui.component.selection.SongSelectionState
import com.alzimer.echobox.ui.icon.SimpIcons
import com.alzimer.echobox.ui.icon.Sync
import com.alzimer.echobox.ui.theme.typo
import com.alzimer.echobox.viewModel.LocalFilesViewModel
import com.alzimer.echobox.viewModel.SharedViewModel
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import echobox.composeapp.generated.resources.Res
import echobox.composeapp.generated.resources.allow_access
import echobox.composeapp.generated.resources.local_files
import echobox.composeapp.generated.resources.local_files_count
import echobox.composeapp.generated.resources.local_files_empty
import echobox.composeapp.generated.resources.local_files_permission_hint
import echobox.composeapp.generated.resources.rescan

/**
 * The "Local files" library chip. The list is just the `song` rows the scanner wrote — it is
 * already reactive, so selecting the chip needs no fetch. A tap builds a queue from the whole
 * list and hands it to the player like a playlist; a rescan is offered both here and in
 * Settings.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalFilesTab(
    contentPadding: PaddingValues,
    navController: NavController,
    selectionState: SongSelectionState,
    onScrolling: (Boolean) -> Unit = {},
    viewModel: LocalFilesViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
) {
    val localFilesLabel = stringResource(Res.string.local_files)
    val rescanLabel = stringResource(Res.string.rescan)
    val songs by viewModel.localSongs.collectAsStateWithLifecycle()
    val scanning by viewModel.scanning.collectAsStateWithLifecycle()
    val playingTrack by sharedViewModel.nowPlayingState.map { it?.songEntity }.collectAsState(initial = null)
    val isPlaying by sharedViewModel.controllerState.map { it.isPlaying }.collectAsState(initial = false)

    var granted by remember { mutableStateOf(false) }
    val permissionRequester =
        rememberLocalAudioPermission { result ->
            granted = result
            if (result) viewModel.rescan()
        }

    // The row's ⋮ opens the same per-song sheet every other list gets.
    var moreSong by remember { mutableStateOf<SongEntity?>(null) }
    if (moreSong != null) {
        NowPlayingBottomSheet(
            onDismiss = { moreSong = null },
            navController = navController,
            song = moreSong,
        )
    }

    // Grant can also change silently — the user flipping it in system settings while the app sits
    // in the recents — so it is re-read on every resume, not just after the dialog answers.
    LifecycleResumeEffect(permissionRequester) {
        val now = permissionRequester.isGranted()
        if (now != granted) granted = now
        onPauseOrDispose { }
    }

    // First run after a grant: an empty table is almost always "never scanned", not "no music".
    LaunchedEffect(granted) {
        if (granted && songs.isEmpty() && !scanning) viewModel.rescan()
    }

    if (!granted) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(Res.string.local_files_permission_hint),
                style = typo().bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { permissionRequester.requestIfNeeded() }) {
                Text(stringResource(Res.string.allow_access))
            }
        }
        return
    }

    val state = rememberLazyListState()
    val isScrollingUp by state.isScrollingUp()
    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleItemIndex }
            .collect {
                if (it <= 1) {
                    onScrolling.invoke(true)
                } else {
                    onScrolling.invoke(isScrollingUp)
                }
            }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        state = state,
    ) {
        item {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = pluralStringResource(Res.plurals.local_files_count, songs.size, songs.size),
                    style = typo().bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .weight(1f)
                            // Polite live region: a rescan that only changes the count still
                            // announces its result to a screen reader.
                            .semantics { liveRegion = LiveRegionMode.Polite },
                )
                // The button's semantics node stays in the tree while scanning — swapping it
                // for the spinner would drop a TalkBack user's focus mid-action. The spinner
                // just covers the (alpha-hidden) icon.
                Box(contentAlignment = Alignment.Center) {
                    RippleIconButton(
                        imageVector = SimpIcons.Sync,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        // RippleIconButton hardcodes no label — set it semantically here.
                        modifier =
                            Modifier
                                .semantics {
                                    contentDescription = rescanLabel
                                }.alpha(if (scanning) 0f else 1f),
                    ) {
                        if (!scanning) viewModel.rescan()
                    }
                    if (scanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }
        }
        if (songs.isEmpty() && !scanning) {
            item {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.local_files_empty),
                        style = typo().bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        items(
            count = songs.size,
            key = { index -> songs[index].videoId },
        ) { index ->
            val item = songs[index]
            SongFullWidthItems(
                songEntity = item,
                isPlaying = playingTrack?.videoId == item.videoId && isPlaying,
                onMoreClickListener = { videoId ->
                    moreSong = songs.find { it.videoId == videoId }
                },
                onClickListener = {
                    val tracks = songs.map(SongEntity::toTrack)
                    viewModel.setQueueData(
                        QueueData.Data(
                            listTracks = ArrayList(tracks),
                            firstPlayedTrack = item.toTrack(),
                            playlistId = LOCAL_FILES_SOURCE_ID,
                            playlistName = localFilesLabel,
                            playlistType = PlaylistType.PLAYLIST,
                            continuation = null,
                        ),
                    )
                    viewModel.loadMediaItem(
                        item.toTrack(),
                        Config.PLAYLIST_CLICK,
                        index,
                    )
                },
                onAddToQueue = {
                    sharedViewModel.addListToQueue(arrayListOf(item.toTrack()))
                },
                selectionMode = selectionState.isActive,
                isSelected = selectionState.isSelected(item.videoId),
                onLongClick = { selectionState.start(it) },
                onSelectToggle = { selectionState.toggle(it) },
                modifier = Modifier.animateItem(),
            )
        }
        item {
            EndOfPage()
        }
    }
}
