package com.example.spotifay.data

import com.example.spotifay.data.audius.AudiusApi
import com.example.spotifay.data.audius.AudiusConfig
import com.example.spotifay.data.audius.AudiusPlaylist
import com.example.spotifay.data.audius.AudiusTrack

/**
 * Single source of music. No session, no token — Audius serves everything
 * anonymously, so every call here works on a cold start.
 */
class MusicRepository(private val api: AudiusApi) {

    private val appName = AudiusConfig.APP_NAME

    suspend fun trendingTracks(limit: Int = 40): List<AudiusTrack> =
        api.trendingTracks(appName, limit, "week").data.orEmpty().filter { it.isPlayable }

    suspend fun searchTracks(query: String, limit: Int = 25): List<AudiusTrack> =
        api.searchTracks(query, appName, limit).data.orEmpty().filter { it.isPlayable }

    suspend fun trendingPlaylists(limit: Int = 20): List<AudiusPlaylist> =
        api.trendingPlaylists(appName, limit).data.orEmpty()

    suspend fun searchPlaylists(query: String, limit: Int = 10): List<AudiusPlaylist> =
        api.searchPlaylists(query, appName, limit).data.orEmpty()

    suspend fun playlistTracks(playlistId: String): List<AudiusTrack> =
        api.playlistTracks(playlistId, appName).data.orEmpty().filter { it.isPlayable }
}