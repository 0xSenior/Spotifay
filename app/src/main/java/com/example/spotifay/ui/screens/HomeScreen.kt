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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.spotifay.ui.components.ArtworkTile
import com.example.spotifay.ui.components.GradientBanner
import com.example.spotifay.ui.components.SectionHeader
import com.example.spotifay.ui.components.SpotifayWordmark
import com.example.spotifay.ui.components.TrackRow
import com.example.spotifay.ui.theme.artworkBrush

private data class ShelfItem(val title: String, val subtitle: String, val seed: Int)

private val madeForYou = listOf(
    ShelfItem("Daily Mix 1", "Spotifay", 1),
    ShelfItem("Late Night Drive", "Spotifay", 2),
    ShelfItem("Deep Focus", "Spotifay", 3),
    ShelfItem("Arab Indie Now", "Spotifay", 4),
    ShelfItem("Warm Up", "Spotifay", 5),
)

private val recentlyPlayed = listOf(
    ShelfItem("Neon Skyline", "Aya Farid", 6),
    ShelfItem("Low Tide", "Coast Signal", 7),
    ShelfItem("Midnight Souq", "Zayn Radi", 8),
    ShelfItem("Paper Planes", "Hana Vale", 9),
)

@Composable
fun HomeScreen() {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SpotifayWordmark(Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            Text(
                text = "Good evening",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
        }

        item {
            GradientBanner(
                title = "Your 2026 rewind",
                subtitle = "The sounds that shaped your year",
                seed = 11,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }

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
            SectionHeader("Made for you", action = "See all")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(madeForYou) { item ->
                    ArtworkTile(
                        seed = item.seed,
                        title = item.title,
                        subtitle = item.subtitle,
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            SectionHeader("Recently played")
        }

        items(recentlyPlayed) { item ->
            TrackRow(
                seed = item.seed,
                title = item.title,
                subtitle = item.subtitle,
                trailing = {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
        }
    }
}

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
                .background(artworkBrush(seed)),
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