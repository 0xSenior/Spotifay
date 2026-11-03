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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotifay.Graph
import com.example.spotifay.data.audius.AudiusTrack
import com.example.spotifay.ui.components.CoverArt
import com.example.spotifay.ui.search.SearchUiState
import com.example.spotifay.ui.search.SearchViewModel
import com.example.spotifay.ui.theme.Violet
import com.example.spotifay.ui.theme.artworkBrush
import kotlinx.coroutines.launch

private val browseGenres = listOf(
    "Electronic", "Hip-Hop", "Lo-Fi", "House", "Ambient", "Jazz",
    "Rock", "Techno", "Trap", "Classical", "R&B", "Drum & Bass",
)

@Composable
fun SearchScreen(
    onPlay: (List<AudiusTrack>, Int) -> Unit,
) {
    val viewModel: SearchViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 10.dp),
        )

        TextField(
            value = text,
            onValueChange = {
                text = it
                viewModel.onQueryChange(it)
            },
            singleLine = true,
            placeholder = { Text("Songs, artists, playlists", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (text.isNotEmpty()) {
                    IconButton(onClick = {
                        text = ""
                        viewModel.onQueryChange("")
                    }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Violet,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(14.dp))

        when (val current = state) {
            SearchUiState.Idle -> BrowseAll(onGenreClick = { genre ->
                text = genre
                viewModel.onQueryChange(genre)
            })

            SearchUiState.Loading -> Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = Violet, strokeWidth = 2.dp) }

            is SearchUiState.Failed -> Box(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            ) {
                Text(current.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            is SearchUiState.Results -> {
                if (current.isEmpty) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    ) {
                        Text(
                            "Nothing found for \"$text\"",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                        if (current.playlists.isNotEmpty()) {
                            item {
                                Text(
                                    "Playlists",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(start = 20.dp, top = 6.dp, bottom = 8.dp),
                                )
                            }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(current.playlists, key = { it.id }) { playlist ->
                                        Column(
                                            Modifier
                                                .width(132.dp)
                                                .clickable {
                                                    scope.launch {
                                                        val tracks = runCatching {
                                                            Graph.repository.playlistTracks(playlist.id)
                                                        }.getOrDefault(emptyList())
                                                        if (tracks.isNotEmpty()) onPlay(tracks, 0)
                                                    }
                                                },
                                        ) {
                                            CoverArt(playlist.thumbUrl, playlist.playlist_name.hashCode(), 132.dp, corner = 14.dp)
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                playlist.playlist_name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                "${playlist.track_count} tracks",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (current.tracks.isNotEmpty()) {
                            item {
                                Text(
                                    "Songs",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 8.dp),
                                )
                            }
                            itemsIndexed(current.tracks, key = { _, track -> track.id }) { index, track ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { onPlay(current.tracks, index) }
                                        .padding(horizontal = 20.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    CoverArt(track.thumbUrl, track.id.hashCode(), 52.dp)
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
                                            "${track.user.name} Â· ${track.playsLabel} plays",
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
                }
            }
        }
    }
}

@Composable
private fun BrowseAll(onGenreClick: (String) -> Unit) {
    Text(
        text = "Browse all",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(browseGenres) { genre ->
            Box(
                Modifier
                    .height(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(artworkBrush(genre.hashCode()))
                    .clickable { onGenreClick(genre) },
            ) {
                Text(
                    text = genre,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                )
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Violet.copy(alpha = 0.35f)),
                )
            }
        }
    }
}