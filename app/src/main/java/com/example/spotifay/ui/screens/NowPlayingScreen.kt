package com.example.spotifay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.spotifay.playback.PlayerHub
import com.example.spotifay.ui.theme.BrandBrush
import com.example.spotifay.ui.theme.OnAccent
import com.example.spotifay.ui.theme.Violet
import com.example.spotifay.ui.theme.artworkBrush

@Composable
fun NowPlayingScreen(onClose: () -> Unit) {
    val state by PlayerHub.state.collectAsStateWithLifecycle()
    var dragPosition by remember { mutableFloatStateOf(-1f) }

    val sliderValue = if (dragPosition >= 0f) dragPosition else state.progress

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                "Now playing",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.size(48.dp))
        }

        Spacer(Modifier.size(28.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (state.artwork.isNullOrBlank()) {
                        artworkBrush(state.title.hashCode())
                    } else {
                        BrandBrush
                    },
                ),
        ) {
            if (!state.artwork.isNullOrBlank()) {
                AsyncImage(
                    model = state.artwork,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }

        Spacer(Modifier.size(28.dp))

        Text(
            text = state.title.ifBlank { "Nothing playing" },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text = state.artist.ifBlank { "Pick a track from Listen now" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.size(20.dp))

        Slider(
            value = sliderValue,
            onValueChange = { dragPosition = it },
            onValueChangeFinished = {
                if (state.durationMs > 0L) {
                    PlayerHub.seekTo((dragPosition * state.durationMs).toLong())
                }
                dragPosition = -1f
            },
            enabled = state.durationMs > 0L,
            colors = SliderDefaults.colors(
                thumbColor = Violet,
                activeTrackColor = Violet,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(state.positionLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(state.durationLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.size(22.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { PlayerHub.previous() }, enabled = state.hasPrevious) {
                Icon(
                    Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous",
                    tint = if (state.hasPrevious) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(38.dp),
                )
            }
            Spacer(Modifier.size(18.dp))
            Box(
                Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(50))
                    .background(BrandBrush),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = { PlayerHub.togglePlayPause() }) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint = OnAccent,
                        modifier = Modifier.size(38.dp),
                    )
                }
            }
            Spacer(Modifier.size(18.dp))
            IconButton(onClick = { PlayerHub.next() }, enabled = state.hasNext) {
                Icon(
                    Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint = if (state.hasNext) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(38.dp),
                )
            }
        }

        if (state.isBuffering) {
            Spacer(Modifier.size(10.dp))
            Text(
                "Buffering…",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (state.error != null) {
            Spacer(Modifier.size(10.dp))
            Text(
                state.error ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}