package com.alzimer.echobox.expect

actual fun getDownloadFolderPath(): String = System.getProperty("user.home") + "/Downloads"