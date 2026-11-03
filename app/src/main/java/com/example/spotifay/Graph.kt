package com.example.spotifay

import android.content.Context
import com.example.spotifay.data.MusicRepository
import com.example.spotifay.data.audius.AudiusApi
import com.example.spotifay.data.audius.AudiusConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Minimal service locator. One place builds the network stack and repository,
 * so no screen has to know how any of it is wired.
 */
object Graph {

    lateinit var api: AudiusApi
        private set

    lateinit var repository: MusicRepository
        private set

    fun init(context: Context) {
        if (this::repository.isInitialized) return

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }

        val client = OkHttpClient.Builder()
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
            .baseUrl(AudiusConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AudiusApi::class.java)

        repository = MusicRepository(api)
    }
}