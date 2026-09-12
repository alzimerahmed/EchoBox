package com.alzimer.echobox.data.di

import com.alzimer.echobox.common.Config.SERVICE_SCOPE
import com.alzimer.echobox.data.io.fileDir
import com.alzimer.echobox.data.repository.AccountRepositoryImpl
import com.alzimer.echobox.data.repository.AlbumRepositoryImpl
import com.alzimer.echobox.data.repository.AnalyticsRepositoryImpl
import com.alzimer.echobox.data.repository.ArtistRepositoryImpl
import com.alzimer.echobox.data.repository.AutoEqRepositoryImpl
import com.alzimer.echobox.data.lyrics.LyricsRomanizerRepositoryImpl
import com.alzimer.echobox.data.repository.CommonRepositoryImpl
import com.alzimer.echobox.data.repository.HomeRepositoryImpl
import com.alzimer.echobox.data.repository.ImportRepositoryImpl
import com.alzimer.echobox.data.repository.LocalPlaylistRepositoryImpl
import com.alzimer.echobox.data.repository.LyricsCanvasRepositoryImpl
import com.alzimer.echobox.data.repository.PlaylistRepositoryImpl
import com.alzimer.echobox.data.repository.PodcastRepositoryImpl
import com.alzimer.echobox.data.repository.SearchRepositoryImpl
import com.alzimer.echobox.data.repository.SongRepositoryImpl
import com.alzimer.echobox.data.repository.StreamRepositoryImpl
import com.alzimer.echobox.data.repository.UpdateRepositoryImpl
import com.alzimer.echobox.domain.repository.AccountRepository
import com.alzimer.echobox.domain.repository.AlbumRepository
import com.alzimer.echobox.domain.repository.AnalyticsRepository
import com.alzimer.echobox.domain.repository.ArtistRepository
import com.alzimer.echobox.domain.repository.AutoEqRepository
import com.alzimer.echobox.domain.repository.LyricsRomanizerRepository
import com.alzimer.echobox.domain.repository.CommonRepository
import com.alzimer.echobox.domain.repository.HomeRepository
import com.alzimer.echobox.domain.repository.ImportRepository
import com.alzimer.echobox.domain.repository.LocalPlaylistRepository
import com.alzimer.echobox.domain.repository.LyricsCanvasRepository
import com.alzimer.echobox.domain.repository.PlaylistRepository
import com.alzimer.echobox.domain.repository.PodcastRepository
import com.alzimer.echobox.domain.repository.SearchRepository
import com.alzimer.echobox.domain.repository.SongRepository
import com.alzimer.echobox.domain.repository.StreamRepository
import com.alzimer.echobox.domain.repository.UpdateRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule =
    module {
        single<AccountRepository>(createdAtStart = true) {
            AccountRepositoryImpl(get(), get())
        }

        single<AlbumRepository>(createdAtStart = true) {
            AlbumRepositoryImpl(get(), get())
        }

        single<ArtistRepository>(createdAtStart = true) {
            ArtistRepositoryImpl(get(), get(), get())
        }

        single<CommonRepository>(createdAtStart = true) {
            CommonRepositoryImpl(get(named(SERVICE_SCOPE)), get(), get(), get(), get(), get()).apply {
                this.init("${fileDir()}/ytdlp-cookie.txt", get())
            }
        }

        // Lazy for the same reason its client is: the picker is the only thing that wants it.
        single<AutoEqRepository> {
            AutoEqRepositoryImpl(get(), get())
        }

        // Lazy: constructing it costs a few File.length() calls, but the kuromoji dictionary
        // behind it is loaded on first Japanese line and never before — so this must NOT be
        // createdAtStart, or every launch pays for a feature most listeners leave off. The path
        // is where Android keeps the downloaded ipadic pack (the APK no longer bundles it);
        // Desktop and iOS ignore it.
        single<LyricsRomanizerRepository> {
            LyricsRomanizerRepositoryImpl("${fileDir()}/kuromoji-ipadic")
        }

        single<HomeRepository>(createdAtStart = true) {
            HomeRepositoryImpl(get(), get())
        }

        single<ImportRepository>(createdAtStart = true) {
            ImportRepositoryImpl(get())
        }

        single<LocalPlaylistRepository>(createdAtStart = true) {
            LocalPlaylistRepositoryImpl(get(), get())
        }

        single<LyricsCanvasRepository>(createdAtStart = true) {
            LyricsCanvasRepositoryImpl(get(), get(), get(), get(), get())
        }

        single<PlaylistRepository>(createdAtStart = true) {
            PlaylistRepositoryImpl(get(), get(), get())
        }

        single<PodcastRepository>(createdAtStart = true) {
            PodcastRepositoryImpl(get(), get())
        }

        single<SearchRepository>(createdAtStart = true) {
            SearchRepositoryImpl(get(), get())
        }

        single<SongRepository>(createdAtStart = true) {
            SongRepositoryImpl(get(), get(), get())
        }

        single<StreamRepository>(createdAtStart = true) {
            StreamRepositoryImpl(get(), get())
        }

        single<UpdateRepository>(createdAtStart = true) {
            UpdateRepositoryImpl(get())
        }

        single<AnalyticsRepository>(createdAtStart = true) {
            AnalyticsRepositoryImpl(get())
        }
    }