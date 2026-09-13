package com.alzimer.echobox.data.di

import com.alzimer.echobox.data.repository.LocalMediaRepositoryImpl
import com.alzimer.echobox.domain.repository.LocalMediaRepository
import org.koin.dsl.module

val localMediaModule =
    module {
        // Lazy, not createdAtStart: nothing touches it until the user enables local files,
        // so most launches should never construct it.
        single<LocalMediaRepository> {
            LocalMediaRepositoryImpl(get(), get(), get())
        }
    }
