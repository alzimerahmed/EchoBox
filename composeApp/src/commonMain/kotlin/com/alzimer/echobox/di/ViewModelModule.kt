package com.alzimer.echobox.di

import com.alzimer.echobox.viewModel.AlbumViewModel
import com.alzimer.echobox.utils.VersionManager
import com.alzimer.echobox.viewModel.AnalyticsViewModel
import com.alzimer.echobox.viewModel.ListenTogetherSettingsViewModel
import com.alzimer.echobox.viewModel.ListenTogetherViewModel
import com.alzimer.echobox.viewModel.ArtistViewModel
import com.alzimer.echobox.viewModel.HomeViewModel
import com.alzimer.echobox.viewModel.ImportViewModel
import com.alzimer.echobox.viewModel.LibraryDynamicPlaylistViewModel
import com.alzimer.echobox.viewModel.LibraryViewModel
import com.alzimer.echobox.viewModel.LocalFilesViewModel
import com.alzimer.echobox.viewModel.LocalPlaylistViewModel
import com.alzimer.echobox.viewModel.LogInViewModel
import com.alzimer.echobox.viewModel.MoodViewModel
import com.alzimer.echobox.viewModel.MoreAlbumsViewModel
import com.alzimer.echobox.viewModel.NotificationViewModel
import com.alzimer.echobox.viewModel.NowPlayingBottomSheetViewModel
import com.alzimer.echobox.viewModel.PlaylistViewModel
import com.alzimer.echobox.viewModel.PodcastViewModel
import com.alzimer.echobox.viewModel.RecentlySongsViewModel
import com.alzimer.echobox.viewModel.SearchViewModel
import com.alzimer.echobox.viewModel.AutoEqViewModel
import com.alzimer.echobox.viewModel.SettingsViewModel
import com.alzimer.echobox.viewModel.SharedViewModel
import com.alzimer.echobox.viewModel.SongSelectionViewModel
import com.alzimer.echobox.feature.wrapped.WrappedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        single {
            SharedViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        single {
            SearchViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            SongSelectionViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            NowPlayingBottomSheetViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LibraryViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LibraryDynamicPlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ImportViewModel(
                get(),
            )
        }
        viewModel {
            AlbumViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            HomeViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            AutoEqViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            SettingsViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ArtistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            PlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LogInViewModel(
                get(),
            )
        }
        viewModel {
            PodcastViewModel(
                get(),
            )
        }
        viewModel {
            MoreAlbumsViewModel(
                get(),
            )
        }
        viewModel {
            RecentlySongsViewModel(
                get(),
            )
        }
        viewModel {
            LocalPlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LocalFilesViewModel(
                get(),
            )
        }
        viewModel {
            NotificationViewModel(
                get(),
            )
        }
        viewModel {
            MoodViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            AnalyticsViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            WrappedViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ListenTogetherSettingsViewModel(get())
        }
        viewModel {
            ListenTogetherViewModel(
                repository = get(),
                dataStore = get(),
                bridge = get(),
            )
        }

    }