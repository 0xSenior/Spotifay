package com.example.spotifay

import android.content.Context
import com.example.spotifay.data.MusicRepository
import com.example.spotifay.data.audius.AudiusApi
import com.example.spotifay.data.audius.AudiusConfig
import com.example.spotifay.data.network.CachePolicyInterceptor
import com.example.spotifay.data.network.CacheTtlInterceptor
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Minimal service locator. One place builds the network stack and repository,
 * so no screen has to know how any of it is wired.
 */
object Graph {

    /** Responses are small JSON, but 48 MB holds days of browsing. */
    private const val API_CACHE_BYTES = 48L * 1024 * 1024

    lateinit var api: AudiusApi
        private set

    lateinit var repository: MusicRepository
        private set

    /** Shared by Retrofit and Coil so both reuse connections and the disk cache. */
    lateinit var httpClient: OkHttpClient
        private set

    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    fun init(context: Context) {
        if (this::repository.isInitialized) return

        httpClient = OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, "api-cache"), API_CACHE_BYTES))
            .addInterceptor(CachePolicyInterceptor())
            .addNetworkInterceptor(CacheTtlInterceptor())
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                        },
                    )
                }
            }
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        api = Retrofit.Builder()
            .baseUrl(AudiusConfig.BASE_URL)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AudiusApi::class.java)

        repository = MusicRepository(api)
    }
}