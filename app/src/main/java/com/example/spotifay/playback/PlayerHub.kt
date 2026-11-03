package com.example.spotifay.playback

import android.content.Context
import com.example.spotifay.data.audius.AudiusTrack
import kotlinx.coroutines.flow.StateFlow

/**
 * Process-wide handle to the player so both the mini player and the full
 * now-playing screen observe the same instance.
 */
object PlayerHub {

    private var connection: PlayerConnection? = null

    fun init(context: Context) {
        if (connection == null) connection = PlayerConnection(context)
    }

    val state: StateFlow<NowPlayingState>
        get() = requireNotNull(connection) { "PlayerHub.init() was never called" }.state

    val recent: StateFlow<List<AudiusTrack>>
        get() = requireNotNull(connection) { "PlayerHub.init() was never called" }.recent

    fun play(tracks: List<AudiusTrack>, startIndex: Int) = connection?.play(tracks, startIndex)

    fun togglePlayPause() = connection?.togglePlayPause()

    fun next() = connection?.next()

    fun previous() = connection?.previous()

    fun seekTo(positionMs: Long) = connection?.seekTo(positionMs)
}