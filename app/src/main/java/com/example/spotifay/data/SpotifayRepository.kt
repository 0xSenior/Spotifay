package com.example.spotifay.data

import com.example.spotifay.data.auth.SpotifyAuth
import com.example.spotifay.data.remote.Album
import com.example.spotifay.data.remote.Artist
import com.example.spotifay.data.remote.Playlist
import com.example.spotifay.data.remote.SearchResponse
import com.example.spotifay.data.remote.SpotifyApi
import com.example.spotifay.data.remote.SpotifyUser
import com.example.spotifay.data.remote.Track

class SpotifayRepository(
    private val api: SpotifyApi,
    private val auth: SpotifyAuth,
) {

    suspend fun currentUser(): SpotifyUser = api.me()

    suspend fun topTracks(limit: Int = 20, timeRange: String = "medium_term"): List<Track> =
        api.topTracks(limit = limit, timeRange = timeRange).items

    suspend fun topArtists(limit: Int = 10): List<Artist> = api.topArtists(limit = limit).items

    suspend fun playlists(limit: Int = 20): List<Playlist> = api.myPlaylists(limit = limit).items

    suspend fun search(query: String): SearchResponse = api.search(query)

    fun isSignedIn(): Boolean = auth.isSignedIn
}