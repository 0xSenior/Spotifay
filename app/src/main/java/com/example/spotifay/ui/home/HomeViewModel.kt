package com.example.spotifay.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifay.Graph
import com.example.spotifay.data.audius.AudiusPlaylist
import com.example.spotifay.data.audius.AudiusTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(
        val tracks: List<AudiusTrack>,
        val playlists: List<AudiusPlaylist>,
    ) : HomeUiState

    data class Failed(val message: String) : HomeUiState
}

class HomeViewModel : ViewModel() {

    private val repository = Graph.repository

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state = _state.asStateFlow()

    init {
        load()
    }

    /**
     * [force] skips the caches; the plain load paints from cache instantly while
     * fresh data (if any) is already being warmed by the repository.
     */
    fun load(force: Boolean = false) {
        viewModelScope.launch {
            if (_state.value !is HomeUiState.Ready) _state.value = HomeUiState.Loading
            runCatching {
                val tracks = repository.trendingTracks(force = force)
                val playlists = runCatching { repository.trendingPlaylists(force = force) }
                    .getOrDefault(emptyList())
                HomeUiState.Ready(tracks = tracks, playlists = playlists)
            }
                .onSuccess { _state.value = it }
                .onFailure { _state.value = HomeUiState.Failed(it.readable()) }
        }
    }
}

/** Turns transport failures into something a person can act on. */
internal fun Throwable.readable(): String = when (this) {
    is HttpException -> when (code()) {
        429 -> "Rate limited by the catalogue. Try again in a moment."
        in 500..599 -> "The catalogue is having a moment (${code()}). Retry."
        else -> "Request failed (${code()})."
    }

    is IOException -> "No network. Check the emulator's connection."
    else -> message ?: "Something went wrong."
}