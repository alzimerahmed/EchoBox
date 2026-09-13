package com.alzimer.echobox.domain.data.entities.analytics

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.alzimer.echobox.domain.extension.now
import kotlinx.datetime.LocalDateTime

// This table grows unbounded — every analytics read filters or sorts on timestamp.
@Entity(tableName = "playback_event", indices = [Index("timestamp")])
data class PlaybackEventEntity(
    @PrimaryKey(autoGenerate = true) val eventId: Long = 0,
    val timestamp: LocalDateTime = now(),
    val videoId: String = "",
    val albumBrowseId: String? = null,
    val durationSecond: Long = 0, // 0 - 100
    val listenedSecond: Long = 0, // in seconds
)