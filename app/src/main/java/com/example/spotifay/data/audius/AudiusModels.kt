package com.example.spotifay.data.audius

import kotlinx.serialization.Serializable

/**
 * Audius is an open music network: no accounts, no API keys, no OAuth.
 * Every listener just identifies themselves with an `app_name` query param.
 */
object AudiusConfig {
    const val BASE_URL = "https://api.audius.co/"
    const val APP_NAME = "Spotifay"

    /** Direct audio stream for a track. Returns audio/mpeg. */
    fun streamUrl(trackId: String): String =
        "${BASE_URL}v1/tracks/$trackId/stream?app_name=$APP_NAME"
}

@Serializable
data class AudiusArtwork(
    val url: String = "",
)

@Serializable
data class AudiusProfilePicture(
    val url: String = "",
)

@Serializable
data class AudiusUser(
    val id: String = "",
    val name: String = "",
    val handle: String = "",
    val follower_count: Int = 0,
    val track_count: Int = 0,
)

@Serializable
data class AudiusTrack(
    val id: String = "",
    val title: String = "",
    val user: AudiusUser = AudiusUser(),
    val artwork: Map<String, String> = emptyMap(),
    val duration: Int = 0,
    val genre: String? = null,
    val mood: String? = null,
    val play_count: Long = 0,
    val favorite_count: Long = 0,
    val release_date: String? = null,
) {
    val coverUrl: String?
        get() = artwork["480x480"] ?: artwork["150x150"] ?: artwork["1000x1000"] ?: artwork.values.firstOrNull()

    val durationLabel: String
        get() {
            val minutes = duration / 60
            val seconds = duration % 60
            return "$minutes:${seconds.toString().padStart(2, '0')}"
        }

    /** 12345 -> "12.3K" */
    val playsLabel: String
        get() = when {
            play_count >= 1_000_000 -> "${"%.1f".format(play_count / 1_000_000.0)}M"
            play_count >= 1_000 -> "${"%.1f".format(play_count / 1_000.0)}K"
            else -> play_count.toString()
        }

    val streamUrl: String get() = AudiusConfig.streamUrl(id)
}

@Serializable
data class AudiusPlaylist(
    val id: String = "",
    val playlist_name: String = "",
    val description: String? = null,
    val artwork: Map<String, String> = emptyMap(),
    val track_count: Int = 0,
    val total_play_count: Long = 0,
    val user: AudiusUser = AudiusUser(),
) {
    val coverUrl: String?
        get() = artwork["480x480"] ?: artwork["150x150"] ?: artwork.values.firstOrNull()
}

@Serializable
data class AudiusEnvelope<T>(
    val data: T? = null,
)