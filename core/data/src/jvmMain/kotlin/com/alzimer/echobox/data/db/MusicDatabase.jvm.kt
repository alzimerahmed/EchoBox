package com.alzimer.echobox.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import com.alzimer.echobox.common.DB_NAME
import com.alzimer.echobox.data.io.getHomeFolderPath
import java.io.File

actual fun getDatabaseBuilder(
    converters: Converters
): RoomDatabase.Builder<MusicDatabase> {
    return Room.databaseBuilder<MusicDatabase>(
        name = getDatabasePath()
    ).addTypeConverter(converters)
}

actual fun getDatabasePath(): String {
    val dbFile = File(getHomeFolderPath(listOf(".echobox", "db")), DB_NAME)
    return dbFile.absolutePath
}