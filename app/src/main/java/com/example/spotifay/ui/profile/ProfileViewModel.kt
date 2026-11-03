package com.example.spotifay.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifay.Graph
import com.example.spotifay.data.remote.SpotifyUser
import com.example.spotifay.ui.home.readable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object SignedOut : ProfileUiState
    data object Loading : ProfileUiState
    data class Ready(val user: SpotifyUser) : ProfileUiState
    data class Failed(val message: String) : ProfileUiState
}

class ProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            Graph.auth.state
                .map { it?.accessToken != null }
                .distinctUntilChanged()
                .collect { signedIn ->
                    if (!signedIn) {
                        _state.value = ProfileUiState.SignedOut
                        return@collect
                    }
                    _state.value = ProfileUiState.Loading
                    runCatching { Graph.repository.currentUser() }
                        .onSuccess { _state.value = ProfileUiState.Ready(it) }
                        .onFailure { _state.value = ProfileUiState.Failed(it.readable()) }
                }
        }
    }

    fun signOut() = Graph.auth.signOut()
}