package com.example.spotifay.data.audius

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AudiusApi {

    @GET("v1/tracks/trending")
    suspend fun trendingTracks(
        @Query("app_name") appName: String,
        @Query("limit") limit: Int,
        @Query("time") time: String,
    ): AudiusEnvelope<List<AudiusTrack>>

    @GET("v1/tracks/search")
    suspend fun searchTracks(
        @Query("query") query: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int,
    ): AudiusEnvelope<List<AudiusTrack>>

    @GET("v1/playlists/trending")
    suspend fun trendingPlaylists(
        @Query("app_name") appName: String,
        @Query("limit") limit: Int,
    ): AudiusEnvelope<List<AudiusPlaylist>>

    @GET("v1/playlists/search")
    suspend fun searchPlaylists(
        @Query("query") query: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int,
    ): AudiusEnvelope<List<AudiusPlaylist>>

    @GET("v1/tracks/{trackId}")
    suspend fun track(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
    ): AudiusEnvelope<AudiusTrack>

    @GET("v1/playlists/{playlistId}/tracks")
    suspend fun playlistTracks(
        @Path("playlistId") playlistId: String,
        @Query("app_name") appName: String,
    ): AudiusEnvelope<List<AudiusTrack>>
}