package com.example.spotifay.playback

import android.content.Context
import com.example.spotifay.data.audius.AudiusTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-wide handle to the player so both the mini player and the full
 * now-playing screen observe the same instance.
 */
object PlayerHub {

    private var connection: PlayerConnection? = null

    private val _preparing = MutableStateFlow(false)

    /** True while a playlist is being fetched right before it starts playing. */
    val preparing: StateFlow<Boolean> = _preparing.asStateFlow()

    fun init(context: Context) {
        if (connection == null) connection = PlayerConnection(context)
    }

    fun setPreparing(value: Boolean) {
        _preparing.value = value
    }

    val state: StateFlow<NowPlayingState>
        get() = requireNotNull(connection) { "PlayerHub.init() was never called" }.state

    /** Track identity only, no playhead — cheap for the app chrome to observe. */
    val trackState: StateFlow<NowPlayingState>
        get() = requireNotNull(connection) { "PlayerHub.init() was never called" }.trackState

    val recent: StateFlow<List<AudiusTrack>>
        get() = requireNotNull(connection) { "PlayerHub.init() was never called" }.recent

    fun play(tracks: List<AudiusTrack>, startIndex: Int) = connection?.play(tracks, startIndex)

    fun togglePlayPause() = connection?.togglePlayPause()

    fun next() = connection?.next()

    fun previous() = connection?.previous()

    fun seekTo(positionMs: Long) = connection?.seekTo(positionMs)
}