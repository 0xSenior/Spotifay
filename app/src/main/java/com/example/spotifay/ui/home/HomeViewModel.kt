package com.example.spotifay.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifay.Graph
import com.example.spotifay.data.remote.Playlist
import com.example.spotifay.data.remote.SpotifyUser
import com.example.spotifay.data.remote.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface HomeUiState {
    data object SignedOut : HomeUiState
    data object Loading : HomeUiState
    data class Ready(
        val user: SpotifyUser,
        val topTracks: List<Track>,
        val playlists: List<Playlist>,
    ) : HomeUiState

    data class Failed(val message: String) : HomeUiState
}

class HomeViewModel : ViewModel() {

    private val repository = Graph.repository

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            Graph.auth.state
                .map { it?.accessToken != null }
                .distinctUntilChanged()
                .collect { signedIn ->
                    if (signedIn) load() else _state.value = HomeUiState.SignedOut
                }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.value = HomeUiState.Loading
            runCatching {
                val user = repository.currentUser()
                val tracks = repository.topTracks()
                val playlists = runCatching { repository.playlists() }.getOrDefault(emptyList())
                HomeUiState.Ready(user, tracks, playlists)
            }.onSuccess { _state.value = it }
                .onFailure { _state.value = HomeUiState.Failed(it.readable()) }
        }
    }

    fun signOut() = Graph.auth.signOut()
}

/** Turns HTTP/IO failures into something a person can act on. */
internal fun Throwable.readable(): String = when (this) {
    is HttpException -> when (code()) {
        401 -> "Session expired. Please sign in again."
        403 -> "Spotify refused this request (403). Check that your account is added under User Management in the developer dashboard."
        404 -> "Nothing found."
        429 -> "Spotify rate limit hit. Try again in a moment."
        else -> "Spotify API error ${code()}."
    }
    is IOException -> "Network unreachable. Check the emulator's connection."
    else -> message ?: "Something went wrong."
}