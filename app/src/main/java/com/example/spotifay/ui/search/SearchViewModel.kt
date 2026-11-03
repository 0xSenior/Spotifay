package com.example.spotifay.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifay.Graph
import com.example.spotifay.data.audius.AudiusPlaylist
import com.example.spotifay.data.audius.AudiusTrack
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
    data object Loading : SearchUiState
    data class Results(
        val tracks: List<AudiusTrack> = emptyList(),
        val playlists: List<AudiusPlaylist> = emptyList(),
    ) : SearchUiState {
        val isEmpty: Boolean get() = tracks.isEmpty() && playlists.isEmpty()
    }

    data class Failed(val message: String) : SearchUiState
}

@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {

    private val query = MutableStateFlow("")

    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            query
                .debounce(350)
                .distinctUntilChanged()
                .collectLatest { raw ->
                    val text = raw.trim()
                    if (text.isEmpty()) {
                        _state.value = SearchUiState.Idle
                        return@collectLatest
                    }
                    _state.value = SearchUiState.Loading
                    runCatching {
                        val tracks = Graph.repository.searchTracks(text)
                        val playlists = runCatching { Graph.repository.searchPlaylists(text) }.getOrDefault(emptyList())
                        SearchUiState.Results(tracks = tracks, playlists = playlists)
                    }
                        .onSuccess { _state.value = it }
                        .onFailure { _state.value = SearchUiState.Failed(it.readable()) }
                }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }
}