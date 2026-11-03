package com.example.spotifay.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.spotifay.Graph
import com.example.spotifay.data.auth.SpotifyAuth

class SpotifySignIn(
    val onSignIn: () -> Unit,
    val error: String?,
    val dismissError: () -> Unit,
)

/**
 * Wires the PKCE round-trip into Compose: launching the Custom Tab and
 * completing the token exchange when the `spotifay://callback` redirect lands.
 */
@Composable
fun rememberSpotifySignIn(): SpotifySignIn {
    var error by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        Graph.auth.handleAuthResponse(result.resultCode, result.data) { failure ->
            error = when (failure) {
                null -> null
                is SpotifyAuth.CancellationExceptionMarker -> null
                else -> failure.message ?: "Sign-in failed"
            }
        }
    }

    return SpotifySignIn(
        onSignIn = {
            error = null
            runCatching { launcher.launch(Graph.auth.loginIntent()) }
                .onFailure { error = it.message ?: "Could not open Spotify sign-in" }
        },
        error = error,
        dismissError = { error = null },
    )
}