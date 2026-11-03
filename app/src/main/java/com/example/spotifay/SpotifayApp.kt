package com.example.spotifay

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.example.spotifay.playback.PlayerHub
import okio.Path.Companion.toOkioPath

private const val IMAGE_CACHE_BYTES = 256L * 1024 * 1024

class SpotifayApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Graph.init(this)
        PlayerHub.init(this)

        // Start filling the catalogue cache before the first screen asks for it.
        Graph.repository.warmUp()

        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components {
                    // Shares the API client, so connections are reused.
                    add(OkHttpNetworkFetcherFactory(callFactory = { Graph.httpClient }))
                }
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizePercent(context, 0.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(context.cacheDir.resolve("image-cache").toOkioPath())
                        .maxSizeBytes(IMAGE_CACHE_BYTES)
                        .build()
                }
                .crossfade(120)
                .build()
        }
    }
}