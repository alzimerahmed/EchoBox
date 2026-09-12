package com.alzimer.echobox.data.io

import com.alzimer.echobox.data.db.documentDirectory
import okio.FileSystem

actual fun fileSystem(): FileSystem = FileSystem.SYSTEM
actual fun fileDir(): String = documentDirectory() + "/EchoBox"