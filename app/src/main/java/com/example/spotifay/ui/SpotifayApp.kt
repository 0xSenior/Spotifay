package com.example.spotifay.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spotifay.data.audius.AudiusTrack
import com.example.spotifay.playback.NowPlayingState
import com.example.spotifay.playback.PlayerHub
import com.example.spotifay.ui.components.CoverArt
import com.example.spotifay.ui.screens.HomeScreen
import com.example.spotifay.ui.screens.LibraryScreen
import com.example.spotifay.ui.screens.NowPlayingScreen
import com.example.spotifay.ui.screens.ProfileScreen
import com.example.spotifay.ui.screens.SearchScreen

const val NowPlayingRoute = "now_playing"

private data class SpotifayTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val tabs = listOf(
    SpotifayTab("home", "Home", Icons.Rounded.Home),
    SpotifayTab("search", "Search", Icons.Rounded.Search),
    SpotifayTab("library", "Library", Icons.Rounded.LibraryMusic),
    SpotifayTab("profile", "You", Icons.Rounded.Person),
)

@Composable
fun SpotifayApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showChrome = currentRoute != NowPlayingRoute

    // Only track identity here: the position tick stays inside Now Playing.
    val playerState by PlayerHub.trackState.collectAsStateWithLifecycle()

    val openNowPlaying: () -> Unit = {
        navController.navigate(NowPlayingRoute) { launchSingleTop = true }
    }

    val onPlay: (List<AudiusTrack>, Int) -> Unit = { tracks, index ->
        PlayerHub.play(tracks, index)
        openNowPlaying()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showChrome) {
                Column {
                    if (playerState.hasTrack) {
                        MiniPlayerBar(state = playerState, onOpen = openNowPlaying)
                    }
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 0.dp,
                    ) {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = {
                                    if (currentRoute != tab.route) {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onBackground,
                                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                ),
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") { HomeScreen(onPlay = onPlay) }
            composable("search") { SearchScreen(onPlay = onPlay) }
            composable("library") { LibraryScreen(onPlay = onPlay) }
            composable("profile") { ProfileScreen() }
            composable(NowPlayingRoute) {
                NowPlayingScreen(onClose = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun MiniPlayerBar(
    state: NowPlayingState,
    onOpen: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoverArt(
                url = state.artwork,
                seed = state.trackId.hashCode(),
                size = 44.dp,
                corner = 10.dp,
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = state.title.ifBlank { "Now playing" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.artist.ifBlank { "Spotifay" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = { PlayerHub.togglePlayPause() }) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(onClick = { PlayerHub.next() }, enabled = state.hasNext) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint = if (state.hasNext) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}