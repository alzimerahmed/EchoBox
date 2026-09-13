package com.alzimer.echobox.data.di.loader

import com.alzimer.echobox.data.di.localMediaModule
import com.alzimer.echobox.media3.di.loadMediaService
import org.koin.core.context.loadKoinModules

actual fun loadMediaService() {
    loadMediaService()
    // Rides this hook because it already exists to load Android-only Koin modules
    // from commonMain's loader.
    loadKoinModules(localMediaModule)
}