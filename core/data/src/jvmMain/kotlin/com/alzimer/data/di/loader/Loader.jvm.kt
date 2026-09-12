package com.alzimer.echobox.data.di.loader

import com.echobox.media_jvm.di.loadDesktopPlayerModule

actual fun loadMediaService() {
    loadDesktopPlayerModule()
}
