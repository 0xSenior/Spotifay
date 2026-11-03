package com.example.spotifay.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class SpotifyImage(
    val url: String = "",
    val height: Int? = null,
    val width: Int? = null,
)

@Serializable
data class Followers(
    val total: Int = 0,
)

@Serializable
data class SpotifyUser(
    val id: String = "",
    val display_name: String? = null,
    val email: String? = null,
    val country: String? = null,
    val product: String? = null,
    val images: List<SpotifyImage> = emptyList(),
    val followers: Followers? = null,
) {
    val avatarUrl: String? get() = images.firstOrNull()?.url
}

@Serializable
data class Artist(
    val id: String = "",
    val name: String = "",
    val images: List<SpotifyImage> = emptyList(),
    val genres: List<String> = emptyList(),
)

@Serializable
data class Album(
    val id: String = "",
    val name: String = "",
    val album_type: String? = null,
    val release_date: String? = null,
    val images: List<SpotifyImage> = emptyList(),
    val artists: List<Artist> = emptyList(),
) {
    val coverUrl: String? get() = images.firstOrNull()?.url
}

@Serializable
data class Track(
    val id: String = "",
    val name: String = "",
    val uri: String = "",
    val duration_ms: Long = 0L,
    val explicit: Boolean = false,
    val artists: List<Artist> = emptyList(),
    val album: Album? = null,
) {
    val artistLine: String get() = artists.joinToString(", ") { it.name }
    val durationLabel: String
        get() {
            val totalSeconds = duration_ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "$minutes:${seconds.toString().padStart(2, '0')}"
        }
}

@Serializable
data class Playlist(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val images: List<SpotifyImage> = emptyList(),
    val owner: UserRef? = null,
) {
    val coverUrl: String? get() = images.firstOrNull()?.url
}

@Serializable
data class UserRef(
    val id: String = "",
    val display_name: String? = null,
)

@Serializable
data class Paging<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0,
    val next: String? = null,
)

@Serializable
data class SearchResponse(
    val tracks: Paging<Track>? = null,
    val artists: Paging<Artist>? = null,
    val albums: Paging<Album>? = null,
    val playlists: Paging<Playlist>? = null,
)

@Serializable
data class CurrentlyPlaying(
    val is_playing: Boolean = false,
    val progress_ms: Long = 0L,
    val item: Track? = null,
)

@Serializable
data class Device(
    val id: String? = null,
    val name: String = "",
    val type: String = "",
    val is_active: Boolean = false,
    val volume_percent: Int? = null,
)

@Serializable
data class DevicesResponse(
    val devices: List<Device> = emptyList(),
)