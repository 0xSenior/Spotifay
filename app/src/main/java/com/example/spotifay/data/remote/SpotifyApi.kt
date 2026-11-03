package com.example.spotifay.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface SpotifyApi {

    @GET("v1/me")
    suspend fun me(): SpotifyUser

    @GET("v1/me/top/tracks")
    suspend fun topTracks(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("time_range") timeRange: String = "medium_term",
    ): Paging<Track>

    @GET("v1/me/top/artists")
    suspend fun topArtists(
        @Query("limit") limit: Int = 10,
        @Query("time_range") timeRange: String = "medium_term",
    ): Paging<Artist>

    @GET("v1/me/playlists")
    suspend fun myPlaylists(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
    ): Paging<Playlist>

    @GET("v1/me/player")
    suspend fun playerState(): CurrentlyPlaying?

    @GET("v1/me/player/devices")
    suspend fun devices(): DevicesResponse

    @GET("v1/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String = "track,artist,album",
        @Query("limit") limit: Int = 10,
    ): SearchResponse
}