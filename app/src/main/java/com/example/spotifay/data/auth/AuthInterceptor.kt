package com.example.spotifay.data.auth

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches the Spotify bearer token to every API call, refreshing it first when
 * the stored token has expired. Runs on OkHttp's worker thread, so the blocking
 * refresh never touches the main thread.
 */
class AuthInterceptor(private val auth: SpotifyAuth) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = auth.validAccessTokenBlocking()
            ?: return chain.proceed(chain.request())

        val request = chain.request()
            .newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(request)
    }
}