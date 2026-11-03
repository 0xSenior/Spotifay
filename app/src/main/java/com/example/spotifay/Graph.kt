package com.example.spotifay

import android.content.Context
import com.example.spotifay.data.SpotifayRepository
import com.example.spotifay.data.auth.AuthInterceptor
import com.example.spotifay.data.auth.SpotifyAuth
import com.example.spotifay.data.remote.SpotifyApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Minimal service locator. One place constructs the network stack, the auth
 * session and the repository, so no screen has to know how any of it is built.
 */
object Graph {

    lateinit var auth: SpotifyAuth
        private set

    lateinit var api: SpotifyApi
        private set

    lateinit var repository: SpotifayRepository
        private set

    fun init(context: Context) {
        if (this::repository.isInitialized) return

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }

        auth = SpotifyAuth(context)

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(auth))
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BASIC
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                },
            )
            .build()

        api = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SpotifyApi::class.java)

        repository = SpotifayRepository(api, auth)
    }
}