package com.alzimer.echobox.domain.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.alzimer.echobox.domain.data.entities.DownloadState.STATE_NOT_DOWNLOADED
import com.alzimer.echobox.domain.data.type.RecentlyType
import com.alzimer.echobox.domain.extension.now
import kotlinx.datetime.LocalDateTime

@Entity(
    tableName = "song",
    indices = [
        // Library list: ORDER BY inLibrary DESC
        Index("inLibrary"),
        // Favorites screen: WHERE liked = 1 ORDER BY favoriteAt DESC
        Index("liked", "favoriteAt"),
        // Downloaded screen: WHERE downloadState = … ORDER BY downloadedAt
        Index("downloadState", "downloadedAt"),
        // Most-played + canvas picks: WHERE totalPlayTime > … ORDER BY totalPlayTime DESC
        Index("totalPlayTime"),
        // Correlated cleanup subqueries match songs to their album via song.albumId
        Index("albumId"),
    ],
)
data class SongEntity(
    @PrimaryKey(autoGenerate = false) val videoId: String = "",
    val albumId: String? = null,
    val albumName: String? = null,
    val artistId: List<String>? = null,
    val artistName: List<String>? = null,
    val duration: String,
    val durationSeconds: Int,
    val isAvailable: Boolean,
    val isExplicit: Boolean,
    val likeStatus: String,
    val thumbnails: String? = null,
    val title: String,
    val videoType: String,
    val category: String?,
    val resultType: String?,
    val liked: Boolean = false,
    val totalPlayTime: Long = 0,
    val downloadState: Int = STATE_NOT_DOWNLOADED,
    val favoriteAt: LocalDateTime? = now(),
    val downloadedAt: LocalDateTime? = now(),
    val inLibrary: LocalDateTime = now(),
    val canvasUrl: String? = null,
    val canvasThumbUrl: String? = null,
) : RecentlyType {
    override fun objectType(): RecentlyType.Type = RecentlyType.Type.SONG
}