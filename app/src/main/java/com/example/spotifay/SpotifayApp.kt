package com.example.spotifay

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.example.spotifay.playback.PlayerHub

class SpotifayApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Graph.init(this)
        PlayerHub.init(this)

        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components { add(OkHttpNetworkFetcherFactory()) }
                .crossfade(true)
                .build()
        }
    }
}