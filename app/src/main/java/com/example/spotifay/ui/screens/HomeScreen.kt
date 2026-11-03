package com.example.spotifay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotifay.Graph
import com.example.spotifay.data.audius.AudiusTrack
import com.example.spotifay.ui.components.CoverArt
import com.example.spotifay.ui.components.SectionHeader
import com.example.spotifay.ui.components.SpotifayWordmark
import com.example.spotifay.ui.home.HomeUiState
import com.example.spotifay.ui.home.HomeViewModel
import com.example.spotifay.ui.theme.OnAccent
import com.example.spotifay.ui.theme.Violet
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onPlay: (List<AudiusTrack>, Int) -> Unit,
) {
    val viewModel: HomeViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

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
                IconButton(onClick = { viewModel.load(force = true) }) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = "Reload",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            Text(
                text = "Listen now",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
        }

        when (val current = state) {
            HomeUiState.Loading -> item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 56.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = Violet, strokeWidth = 2.dp) }
            }

            is HomeUiState.Failed -> item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(18.dp),
                ) {
                    Text(
                        "Couldn't reach the catalogue",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        current.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.load(force = true) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                    ) { Text("Retry") }
                }
            }

            is HomeUiState.Ready -> {
                if (current.playlists.isNotEmpty()) {
                    item {
                        SectionHeader("Trending playlists")
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(current.playlists) { playlist ->
                                Column(
                                    Modifier
                                        .width(148.dp)
                                        .clickable {
                                            scope.launch {
                                                val tracks = runCatching {
                                                    Graph.repository.playlistTracks(playlist.id)
                                                }.getOrDefault(emptyList())
                                                if (tracks.isNotEmpty()) onPlay(tracks, 0)
                                            }
                                        },
                                ) {
                                    Box {
                                        CoverArt(
                                            url = playlist.coverUrl,
                                            seed = playlist.playlist_name.hashCode(),
                                            size = 148.dp,
                                            corner = 14.dp,
                                        )
                                        Box(
                                            Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp)
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(Violet),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                Icons.Rounded.PlayArrow,
                                                contentDescription = null,
                                                tint = OnAccent,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        playlist.playlist_name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        "${playlist.track_count} tracks · ${playlist.user.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(14.dp))
                    SectionHeader("Trending tracks", action = "This week")
                }

                itemsIndexed(current.tracks) { index, track ->
                    TrackCard(
                        index = index + 1,
                        track = track,
                        onClick = { onPlay(current.tracks, index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackCard(
    index: Int,
    track: AudiusTrack,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp),
        )
        CoverArt(url = track.coverUrl, seed = track.id.hashCode(), size = 52.dp)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${track.user.name} · ${track.playsLabel} plays",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            track.durationLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}