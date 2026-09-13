package com.alzimer.echobox.data.dataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.alzimer.echobox.common.SETTINGS_FILENAME
import com.alzimer.echobox.data.io.getHomeFolderPath
import createDataStore
import java.io.File

actual fun createDataStoreInstance(): DataStore<Preferences> = createDataStore(
    producePath = {
        val file = File(getHomeFolderPath(listOf(".echobox")), "$SETTINGS_FILENAME.preferences_pb")
        file.absolutePath
    }
)