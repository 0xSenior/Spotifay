package com.example.spotifay.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.spotifay.ui.components.BrandMark
import com.example.spotifay.ui.components.rememberSpotifySignIn
import com.example.spotifay.ui.profile.ProfileUiState
import com.example.spotifay.ui.profile.ProfileViewModel
import com.example.spotifay.ui.theme.BrandBrush
import com.example.spotifay.ui.theme.OnAccent
import com.example.spotifay.ui.theme.Violet

private data class SettingsEntry(val icon: ImageVector, val title: String, val subtitle: String)

private val settingsEntries = listOf(
    SettingsEntry(Icons.Rounded.Download, "Downloads", "Local files only"),
    SettingsEntry(Icons.Rounded.Equalizer, "Playback", "Quality, crossfade, gapless"),
    SettingsEntry(Icons.Rounded.Palette, "Appearance", "Theme and layout"),
    SettingsEntry(Icons.Rounded.Notifications, "Notifications", "New releases, playlists"),
    SettingsEntry(Icons.Rounded.Info, "About Spotifay", "Version 0.1.0"),
)

@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val signIn = rememberSpotifySignIn()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (val current = state) {
                    is ProfileUiState.Ready -> {
                        Box(
                            Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(50))
                                .background(BrandBrush),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (current.user.avatarUrl != null) {
                                AsyncImage(
                                    model = current.user.avatarUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize(),
                                )
                            } else {
                                BrandMark(size = 48.dp)
                            }
                        }
                        Spacer(Modifier.size(14.dp))
                        Text(
                            current.user.display_name ?: current.user.id,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            current.user.email ?: current.user.id,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (!current.user.product.isNullOrBlank()) {
                            Spacer(Modifier.size(8.dp))
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Violet.copy(alpha = 0.18f))
                                    .padding(horizontal = 12.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    current.user.product!!.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Violet,
                                )
                            }
                        }
                    }

                    ProfileUiState.Loading -> Box(
                        Modifier.size(88.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = Violet, strokeWidth = 2.dp) }

                    ProfileUiState.SignedOut -> {
                        Box(
                            Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(50))
                                .background(BrandBrush),
                            contentAlignment = Alignment.Center,
                        ) { BrandMark(size = 48.dp) }
                        Spacer(Modifier.size(14.dp))
                        Text(
                            "Guest",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            "Not signed in",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    is ProfileUiState.Failed -> {
                        Text(
                            "Couldn't load profile",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            current.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        if (state is ProfileUiState.SignedOut || state is ProfileUiState.Failed) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        onClick = signIn.onSignIn,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Violet, contentColor = OnAccent),
                    ) { Text("Sign in with Spotify") }
                    if (signIn.error != null) {
                        Spacer(Modifier.size(8.dp))
                        Text(signIn.error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        if (state is ProfileUiState.Ready) {
            item {
                Spacer(Modifier.size(6.dp))
                Button(
                    onClick = { viewModel.signOut() },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Icon(Icons.Rounded.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Sign out")
                }
            }
        }

        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 4.dp),
            )
        }

        items(settingsEntries) { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        entry.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.size(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        entry.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}