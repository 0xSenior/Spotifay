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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotifay.Graph
import com.example.spotifay.data.audius.AudiusPlaylist
import com.example.spotifay.data.audius.AudiusTrack
import com.example.spotifay.playback.PlayerHub
import com.example.spotifay.ui.components.CoverArt
import com.example.spotifay.ui.components.SectionHeader
import com.example.spotifay.ui.theme.Violet
import kotlinx.coroutines.launch

/**
 * The library surfaces what this device actually played (kept in memory for the
 * session) plus the catalogue's current playlists. Nothing here is a mock.
 */
@Composable
fun LibraryScreen(
    onPlay: (List<AudiusTrack>, Int) -> Unit,
) {
    val recent by PlayerHub.recent.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val playlists by produceState<List<AudiusPlaylist>?>(initialValue = null) {
        value = runCatching { Graph.repository.trendingPlaylists(12) }.getOrDefault(emptyList())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)) {
                Text(
                    text = "Your Library",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Played on this device, plus what is trending now.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (recent.isNotEmpty()) {
            item { SectionHeader("Recently played") }
            itemsIndexed(recent) { index, track ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPlay(recent, index) }
                        .padding(horizontal = 20.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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
                            "${track.user.name} · ${track.durationLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        item { SectionHeader("Trending playlists", action = "Audius") }

        when (val loaded = playlists) {
            null -> item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = Violet, strokeWidth = 2.dp) }
            }

            else -> if (loaded.isEmpty()) {
                item {
                    Text(
                        "Couldn't load playlists. Check the connection and try again.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
            } else {
                items(loaded) { playlist ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    val tracks = runCatching {
                                        Graph.repository.playlistTracks(playlist.id)
                                    }.getOrDefault(emptyList())
                                    if (tracks.isNotEmpty()) onPlay(tracks, 0)
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box {
                            CoverArt(
                                url = playlist.coverUrl,
                                seed = playlist.playlist_name.hashCode(),
                                size = 56.dp,
                                corner = 12.dp,
                            )
                            Box(
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Violet),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.background,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
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
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = "Play playlist",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}