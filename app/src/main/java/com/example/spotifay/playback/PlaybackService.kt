package com.example.spotifay.playback

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

/** ~350 hours of 128 kbps audio; the LRU evictor trims the oldest first. */
private const val AUDIO_CACHE_BYTES = 512L * 1024 * 1024

/** Roughly two minutes of audio, warmed for the next queue item. */
private const val PREFETCH_BYTES = 2L * 1024 * 1024

private const val USER_AGENT = "Spotifay/0.1.0 (Android)"

/**
 * Foreground media service: keeps playback alive with the app in the background
 * and gives the system a media notification / lock-screen controls.
 *
 * Audio is read through a disk cache, and the next track's head is prefetched
 * as soon as the current one starts, so "next" is near-instant.
 */
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var cache: SimpleCache? = null
    private var writer: CacheWriter? = null
    private var prefetchJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val httpFactory = DefaultHttpDataSource.Factory()
        .setUserAgent(USER_AGENT)
        .setConnectTimeoutMs(12_000)
        .setReadTimeoutMs(20_000)
        .setAllowCrossProtocolRedirects(true)

    override fun onCreate() {
        super.onCreate()

        val audioCache = SimpleCache(
            File(cacheDir, "audio-cache"),
            LeastRecentlyUsedCacheEvictor(AUDIO_CACHE_BYTES),
            StandaloneDatabaseProvider(this),
        )
        cache = audioCache

        val cacheFactory = CacheDataSource.Factory()
            .setCache(audioCache)
            .setUpstreamDataSourceFactory(httpFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 15_000,
                /* maxBufferMs = */ 120_000,
                /* bufferForPlaybackMs = */ 1_000,
                /* bufferForPlaybackAfterRebufferMs = */ 2_000,
            )
            .build()

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheFactory))
            .setLoadControl(loadControl)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                prefetchNext(player)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) prefetchNext(player)
            }
        })

        mediaSession = MediaSession.Builder(this, player).build()
    }

    /**
     * Pulls the first few megabytes of the upcoming track into the cache. The
     * player then starts it without touching the network, and a track that was
     * played once is available offline.
     */
    private fun prefetchNext(player: Player) {
        val nextIndex = player.nextMediaItemIndex
        if (nextIndex == C.INDEX_UNSET) return
        val uri = player.getMediaItemAt(nextIndex).localConfiguration?.uri ?: return
        val audioCache = cache ?: return

        writer?.cancel()
        writer = null
        prefetchJob?.cancel()

        prefetchJob = scope.launch {
            runCatching {
                val dataSource = CacheDataSource.Factory()
                    .setCache(audioCache)
                    .setUpstreamDataSourceFactory(httpFactory)
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                    .createDataSourceForDownloading()

                val spec = DataSpec(uri, 0, PREFETCH_BYTES)
                val running = CacheWriter(dataSource, spec, null, null)
                writer = running
                running.cache()
            }
            writer = null
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        writer?.cancel()
        writer = null
        prefetchJob?.cancel()
        scope.cancel()

        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        cache?.release()
        cache = null
        super.onDestroy()
    }
}