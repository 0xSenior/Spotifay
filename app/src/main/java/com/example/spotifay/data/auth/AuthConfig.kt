package com.example.spotifay.data.auth

/**
 * Spotify application credentials.
 *
 * The client secret is intentionally absent: this is a public (mobile) client and
 * authorisation uses PKCE, so no secret ever ships inside the APK.
 */
object AuthConfig {
    const val CLIENT_ID = "467b5588695e4268af19cec786eb1190"
    const val REDIRECT_URI = "spotifay://callback"

    const val AUTHORIZATION_ENDPOINT = "https://accounts.spotify.com/authorize"
    const val TOKEN_ENDPOINT = "https://accounts.spotify.com/api/token"

    val SCOPES = listOf(
        "user-read-private",
        "user-read-email",
        "user-top-read",
        "user-read-recently-played",
        "user-library-read",
        "playlist-read-private",
        "playlist-read-collaborative",
        "user-read-playback-state",
        "user-modify-playback-state",
        "user-read-currently-playing",
        "streaming",
        "app-remote-control",
    )
}