package com.example.spotifay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.spotifay.playback.PlayerHub
import com.example.spotifay.ui.components.CoverArt
import com.example.spotifay.ui.components.LoadingBar
import com.example.spotifay.ui.components.LoadingCrossfade
import com.example.spotifay.ui.components.SectionHeader
import com.example.spotifay.ui.components.ShimmerHost
import com.example.spotifay.ui.components.SkeletonBox
import com.example.spotifay.ui.components.SkeletonPlaylistCarousel
import com.example.spotifay.ui.components.SkeletonTrackRow
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
    val preparing by PlayerHub.preparing.collectAsStateWithLifecycle()

    LoadingCrossfade(
        loading = state is HomeUiState.Loading,
        modifier = Modifier.fillMaxSize(),
        skeleton = { HomeSkeleton(onReload = { viewModel.load(force = true) }) },
        content = {
            HomeContent(
                state = state,
                preparing = preparing,
                onPlay = onPlay,
                onReload = { viewModel.load(force = true) },
            )
        },
    )
}

/** Placeholders that trace the real Home layout while the catalogue loads. */
@Composable
private fun HomeSkeleton(onReload: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item { HomeHeader(onReload) }

        item {
            Text(
                text = "Listen now",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
        }

        item {
            ShimmerHost {
                Column(Modifier.fillMaxWidth()) {
                    SkeletonBox(
                        Modifier
                            .padding(start = 20.dp, top = 10.dp, bottom = 12.dp)
                            .width(168.dp)
                            .height(20.dp),
                        RoundedCornerShape(7.dp),
                    )
                    SkeletonPlaylistCarousel()
                    SkeletonBox(
                        Modifier
                            .padding(start = 20.dp, top = 22.dp, bottom = 10.dp)
                            .width(146.dp)
                            .height(20.dp),
                        RoundedCornerShape(7.dp),
                    )
                    repeat(7) { SkeletonTrackRow() }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(onReload: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpotifayWordmark(Modifier.weight(1f))
        IconButton(onClick = onReload) {
            Icon(
                Icons.Rounded.Refresh,
                contentDescription = "Reload",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    preparing: Boolean,
    onPlay: (List<AudiusTrack>, Int) -> Unit,
    onReload: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item { HomeHeader(onReload) }

        item { LoadingBar(visible = preparing, modifier = Modifier.padding(horizontal = 20.dp)) }

        item {
            Text(
                text = "Listen now",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
        }

        when (val current = state) {
            // Never visible: the skeleton layer owns this state (see HomeSkeleton).
            HomeUiState.Loading -> Unit

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
                        onClick = onReload,
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
                            items(current.playlists, key = { it.id }) { playlist ->
                                Column(
                                    Modifier
                                        .width(148.dp)
                                        .clickable {
                                            if (PlayerHub.preparing.value) return@clickable
                                            PlayerHub.setPreparing(true)
                                            scope.launch {
                                                try {
                                                    val tracks = runCatching {
                                                        Graph.repository.playlistTracks(playlist.id)
                                                    }.getOrDefault(emptyList())
                                                    if (tracks.isNotEmpty()) onPlay(tracks, 0)
                                                } finally {
                                                    PlayerHub.setPreparing(false)
                                                }
                                            }
                                        },
                                ) {
                                    Box {
                                        CoverArt(
                                            url = playlist.thumbUrl,
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

                itemsIndexed(current.tracks, key = { _, track -> track.id }) { index, track ->
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
        CoverArt(url = track.thumbUrl, seed = track.id.hashCode(), size = 52.dp)
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