package com.example.spotifay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.spotifay.ui.components.TrackRow
import com.example.spotifay.ui.theme.Violet
import com.example.spotifay.ui.theme.artworkBrush

private data class LibraryEntry(val title: String, val meta: String, val seed: Int)

private val entries = listOf(
    LibraryEntry("Liked Songs", "Playlist • 214 songs", 21),
    LibraryEntry("Late Night Drive", "Playlist • 38 songs", 22),
    LibraryEntry("Focus Flow", "Playlist • 91 songs", 23),
    LibraryEntry("Neon Skyline", "Album • Aya Farid", 24),
    LibraryEntry("Coast Signal", "Artist", 25),
    LibraryEntry("Midnight Souq", "Album • Zayn Radi", 26),
)

@Composable
fun LibraryScreen() {
    var selected by remember { mutableIntStateOf(0) }
    val filters = listOf("Playlists", "Artists", "Albums", "Podcasts")

    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Your Library",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 10.dp),
        )

        Row(
            Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            filters.forEachIndexed { index, label ->
                FilterChip(
                    selected = selected == index,
                    onClick = { selected = index },
                    label = { Text(label) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Violet,
                        selectedLabelColor = MaterialTheme.colorScheme.onBackground,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }

        Spacer(Modifier.size(6.dp))

        LazyColumn(
            contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            tint = Violet,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.size(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Liked Songs",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            "Playlist • 214 songs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }

            items(entries.drop(1)) { entry ->
                TrackRow(
                    seed = entry.seed,
                    title = entry.title,
                    subtitle = entry.meta,
                )
            }
        }
    }
}