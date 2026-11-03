package com.example.spotifay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotifay.data.remote.Track
import com.example.spotifay.ui.components.ArtworkTile
import com.example.spotifay.ui.components.CoverArt
import com.example.spotifay.ui.components.GradientBanner
import com.example.spotifay.ui.components.SectionHeader
import com.example.spotifay.ui.components.SpotifayWordmark
import com.example.spotifay.ui.components.TrackRow
import com.example.spotifay.ui.components.rememberSpotifySignIn
import com.example.spotifay.ui.home.HomeUiState
import com.example.spotifay.ui.home.HomeViewModel
import com.example.spotifay.ui.theme.OnAccent
import com.example.spotifay.ui.theme.Violet

@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val signIn = rememberSpotifySignIn()

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SpotifayWordmark(Modifier.weight(1f))
                IconButton(onClick = { viewModel.load() }) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = "Reload",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        when (val current = state) {
            HomeUiState.SignedOut -> {
                item { Greeting("Good evening") }
                item {
                    SignInCard(
                        error = signIn.error,
                        onSignIn = signIn.onSignIn,
                    )
                }
                placeholderSections()
            }

            HomeUiState.Loading -> {
                item { Greeting("Good evening") }
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Violet, strokeWidth = 2.dp)
                    }
                }
            }

            is HomeUiState.Failed -> {
                item { Greeting("Good evening") }
                item { ErrorCard(current.message, onRetry = viewModel::load) }
            }

            is HomeUiState.Ready -> {
                val name = current.user.display_name?.substringBefore(' ') ?: "there"
                item { Greeting("Good evening, $name") }
                item {
                    GradientBanner(
                        title = "${current.user.product?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Spotify"} account",
                        subtitle = "${current.user.followers?.total ?: 0} followers · ${current.user.country ?: "--"}",
                        seed = 11,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }

                if (current.topTracks.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(6.dp))
                        SectionHeader("Your top tracks", action = "Last 6 months")
                    }
                    itemsIndexed(current.topTracks) { index, track ->
                        SongRow(index + 1, track)
                    }
                }

                if (current.playlists.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        SectionHeader("Your playlists", action = "${current.playlists.size}")
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(current.playlists) { playlist ->
                                ArtworkTile(
                                    seed = playlist.name.hashCode(),
                                    title = playlist.name,
                                    subtitle = playlist.owner?.display_name ?: "Playlist",
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(Violet, MaterialTheme.colorScheme.secondary),
                                    ),
                                    url = playlist.coverUrl,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Greeting(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
    )
}

@Composable
private fun SignInCard(error: String?, onSignIn: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(18.dp),
    ) {
        Text(
            "Connect your Spotify account",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Spotifay signs in through Spotify's own consent screen using PKCE. " +
                "Premium is required for full-length playback.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onSignIn,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Violet,
                contentColor = OnAccent,
            ),
        ) {
            Text("Sign in with Spotify")
        }
        if (error != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(18.dp),
    ) {
        Text(
            "Couldn't load your library",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ),
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun SongRow(index: Int, track: Track) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp),
        )
        CoverArt(
            url = track.album?.coverUrl,
            seed = track.name.hashCode(),
            size = 52.dp,
        )
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = track.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
            Text(
                text = track.artistLine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Text(
            text = track.durationLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.placeholderSections() {
    item {
        Spacer(Modifier.height(8.dp))
        SectionHeader("Jump back in")
    }
    item {
        Column(
            Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickChip("Liked Songs", 12, Modifier.weight(1f))
                QuickChip("Daily Mix 1", 13, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickChip("Deep Focus", 14, Modifier.weight(1f))
                QuickChip("Neon Skyline", 15, Modifier.weight(1f))
            }
        }
    }
    item {
        Spacer(Modifier.height(12.dp))
        SectionHeader("Made for you", action = "See all")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(madeForYouSeeds) { seed ->
                ArtworkTile(seed = seed, title = shelfTitles[seed % shelfTitles.size], subtitle = "Spotifay")
            }
        }
    }
    itemsIndexed(recentSeeds) { index, seed ->
        TrackRow(
            seed = seed,
            title = recentTitles[index % recentTitles.size],
            subtitle = "Sign in to see what you actually played",
        )
    }
}

private val madeForYouSeeds = listOf(1, 2, 3, 4, 5)
private val shelfTitles = listOf("Daily Mix 1", "Late Night Drive", "Deep Focus", "Arab Indie Now", "Warm Up")
private val recentSeeds = listOf(6, 7, 8, 9)
private val recentTitles = listOf("Neon Skyline", "Low Tide", "Midnight Souq", "Paper Planes")

@Composable
private fun QuickChip(title: String, seed: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(56.dp)
                .background(com.example.spotifay.ui.theme.artworkBrush(seed)),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            modifier = Modifier.padding(end = 8.dp),
        )
    }
}