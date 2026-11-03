package com.example.spotifay.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifay.Graph
import com.example.spotifay.data.remote.Album
import com.example.spotifay.data.remote.Artist
import com.example.spotifay.data.remote.Track
import com.example.spotifay.ui.home.readable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object NeedsAuth : SearchUiState
    data object Loading : SearchUiState
    data class Results(
        val tracks: List<Track> = emptyList(),
        val artists: List<Artist> = emptyList(),
        val albums: List<Album> = emptyList(),
    ) : SearchUiState {
        val isEmpty: Boolean get() = tracks.isEmpty() && artists.isEmpty() && albums.isEmpty()
    }

    data class Failed(val message: String) : SearchUiState
}

@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {

    private val query = MutableStateFlow("")
    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val state = _state.asStateFlow()

    val currentQuery: String get() = query.value

    init {
        viewModelScope.launch {
            query
                .debounce(350)
                .distinctUntilChanged()
                .collectLatest { text ->
                    val trimmed = text.trim()
                    when {
                        trimmed.isEmpty() -> _state.value = SearchUiState.Idle
                        !Graph.auth.isSignedIn -> _state.value = SearchUiState.NeedsAuth
                        else -> {
                            _state.value = SearchUiState.Loading
                            runCatching { Graph.repository.search(trimmed) }
                                .onSuccess { response ->
                                    _state.value = SearchUiState.Results(
                                        tracks = response.tracks?.items.orEmpty(),
                                        artists = response.artists?.items.orEmpty(),
                                        albums = response.albums?.items.orEmpty(),
                                    )
                                }
                                .onFailure { _state.value = SearchUiState.Failed(it.readable()) }
                        }
                    }
                }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }
}