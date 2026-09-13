package com.alzimer.echobox.domain.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.alzimer.echobox.domain.data.type.ArtistType
import com.alzimer.echobox.domain.data.type.RecentlyType
import com.alzimer.echobox.domain.extension.now
import kotlinx.datetime.LocalDateTime

@Entity(
    tableName = "artist",
    indices = [
        Index("inLibrary"),
        // Followed-artists screen + the unfollow cleanup subqueries both filter on followed
        Index("followed", "followedAt"),
    ],
)
data class ArtistEntity(
    @PrimaryKey(autoGenerate = false)
    val channelId: String,
    val name: String,
    val thumbnails: String?,
    val followed: Boolean = false,
    val followedAt: LocalDateTime? = now(),
    val inLibrary: LocalDateTime = now(),
    // Cached artist name-logo image (hidden catalog) + its dominant color (hex). Nullable so the
    // Room AutoMigration can add the columns without a manual migration.
    val nameLogoUrl: String? = null,
    val nameLogoColor: String? = null,
) : RecentlyType,
    ArtistType {
    override fun objectType() = RecentlyType.Type.ARTIST
}