package com.alzimer.echobox.data.di

import DatabaseDao
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.alzimer.echobox.data.dataStore.DataStoreManagerImpl
import com.alzimer.echobox.data.dataStore.createDataStoreInstance
import com.alzimer.echobox.data.db.Converters
import com.alzimer.echobox.data.db.MusicDatabase
import com.alzimer.echobox.data.db.datasource.AnalyticsDatasource
import com.alzimer.echobox.data.db.datasource.LocalDataSource
import com.alzimer.echobox.data.db.getDatabaseBuilder
import com.alzimer.echobox.domain.manager.DataStoreManager
import com.alzimer.echobox.kotlinytmusicscraper.YouTube
import com.alzimer.echobox.spotify.Spotify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module
import org.simpmusic.aiservice.AiClient
import org.simpmusic.lyrics.EchoBoxLyricsClient
import kotlin.time.ExperimentalTime
import org.simpmusic.autoeq.AutoEq

@OptIn(ExperimentalTime::class)
val databaseModule =
    module {
        single(createdAtStart = true) {
            Converters()
        }
        // Database
        single(createdAtStart = true) {
            getDatabaseBuilder(
                get<Converters>()
            )
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        }
        // DatabaseDao
        single(createdAtStart = true) {
            get<MusicDatabase>().getDatabaseDao()
        }
        // LocalDataSource
        single(createdAtStart = true) {
            LocalDataSource(get<DatabaseDao>(), get<MusicDatabase>())
        }
        // AnalyticsDatasource
        single(createdAtStart = true) {
            AnalyticsDatasource(get<DatabaseDao>())
        }
        // Datastore
        single(createdAtStart = true) {
            createDataStoreInstance()
        }
        // DatastoreManager
        single<DataStoreManager>(createdAtStart = true) {
            DataStoreManagerImpl(get<DataStore<Preferences>>())
        }

        // Move YouTube from Singleton to Koin DI
        single(createdAtStart = true) {
            YouTube()
        }

        single(createdAtStart = true) {
            Spotify()
        }

        single(createdAtStart = true) {
            AiClient()
        }

        single(createdAtStart = true) {
            EchoBoxLyricsClient()
        }

        // Not created at start, unlike the rest: nothing needs it until someone opens the AutoEq
        // picker, and it holds an HTTP client the vast majority of sessions never use.
        single {
            AutoEq()
        }
    }