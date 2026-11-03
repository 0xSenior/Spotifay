package com.example.spotifay.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.spotifay.data.audius.AudiusTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

data class NowPlayingState(
    val title: String = "",
    val artist: String = "",
    val artwork: String? = null,
    val trackId: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val error: String? = null,
) {
    val hasTrack: Boolean get() = trackId != null

    val progress: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    val positionLabel: String get() = format(positionMs)
    val durationLabel: String get() = format(durationMs)

    private fun format(ms: Long): String {
        val totalSeconds = (ms / 1000).coerceAtLeast(0)
        return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
    }
}

/**
 * Owns the connection to [PlaybackService] and mirrors player state into a
 * [StateFlow] the Compose layer can observe.
 */
class PlayerConnection(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val executor = Executor { it.run() }

    private var controller: MediaController? = null
    private var ticker: Job? = null

    private val _state = MutableStateFlow(NowPlayingState())
    val state: StateFlow<NowPlayingState> = _state.asStateFlow()

    init {
        connect()
    }

    private fun connect() {
        val token = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        val future = MediaController.Builder(appContext, token).buildAsync()

        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess { mediaController ->
                        controller = mediaController
                        mediaController.addListener(object : Player.Listener {
                            override fun onEvents(player: Player, events: Player.Events) = publish()
                            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) = publish()
                            override fun onIsPlayingChanged(isPlaying: Boolean) = publish()
                            override fun onPlaybackStateChanged(playbackState: Int) = publish()
                            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = publish()
                            override fun onPlayerError(error: PlaybackException) {
                                _state.value = _state.value.copy(error = error.message ?: "Playback failed")
                            }
                        })
                        publish()
                        startTicker()
                    }
                    .onFailure {
                        _state.value = _state.value.copy(error = "Playback service unavailable")
                    }
            },
            executor,
        )
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (true) {
                val mediaController = controller
                if (mediaController != null && mediaController.isPlaying) {
                    _state.value = _state.value.copy(
                        positionMs = mediaController.currentPosition.coerceAtLeast(0L),
                    )
                }
                delay(500)
            }
        }
    }

    private fun publish() {
        val mediaController = controller ?: return
        val metadata = mediaController.mediaMetadata
        val duration = mediaController.duration

        _state.value = NowPlayingState(
            title = metadata.title?.toString().orEmpty(),
            artist = metadata.artist?.toString().orEmpty(),
            artwork = metadata.artworkUri?.toString(),
            trackId = mediaController.currentMediaItem?.mediaId,
            isPlaying = mediaController.isPlaying,
            isBuffering = mediaController.playbackState == Player.STATE_BUFFERING,
            positionMs = mediaController.currentPosition.coerceAtLeast(0L),
            durationMs = if (duration > 0) duration else 0L,
            hasNext = mediaController.hasNextMediaItem(),
            hasPrevious = mediaController.hasPreviousMediaItem(),
        )
    }

    /** Replaces the queue with [tracks] and starts at [startIndex]. */
    fun play(tracks: List<AudiusTrack>, startIndex: Int) {
        val mediaController = controller ?: return
        if (tracks.isEmpty()) return

        val items = tracks.map { track ->
            MediaItem.Builder()
                .setMediaId(track.id)
                .setUri(track.streamUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.user.name)
                        .apply { track.coverUrl?.let { setArtworkUri(Uri.parse(it)) } }
                        .build(),
                )
                .build()
        }

        val index = startIndex.coerceIn(0, items.lastIndex)
        mediaController.setMediaItems(items, index, 0L)
        runCatching { mediaController.prepare() }
        mediaController.play()
        publish()
    }

    fun togglePlayPause() {
        val mediaController = controller ?: return
        if (mediaController.isPlaying) mediaController.pause() else mediaController.play()
    }

    fun next() = controller?.seekToNextMediaItem()

    fun previous() = controller?.seekToPreviousMediaItem()

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _state.value = _state.value.copy(positionMs = positionMs)
    }

    fun start() {
        val mediaController = controller
        if (mediaController != null && mediaController.mediaItemCount > 0) {
            ContextCompat.startForegroundService(
                appContext,
                android.content.Intent(appContext, PlaybackService::class.java),
            )
        }
    }
}