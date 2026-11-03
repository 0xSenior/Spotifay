package com.example.spotifay.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.spotifay.ui.theme.Violet

/** One sweep of the highlight band, milliseconds. */
private const val SWEEP_MS = 1150

/** How wide the moving highlight is, relative to the tile. */
private const val SWEEP_WIDTH = 0.75f

/**
 * A single animation shared by every skeleton tile on a screen. Tiles read
 * [progress] inside their draw pass, so the sweep repaints without triggering
 * recomposition of anything.
 */
@Stable
class ShimmerState internal constructor(private val progress: State<Float>) {
    fun progress(): Float = progress.value
}

val LocalShimmer = staticCompositionLocalOf<ShimmerState?> { null }

/**
 * Drives the shimmer for its content. When [enabled] is false it costs nothing
 * and simply renders the content, so screens can wrap their whole scroll
 * container once and only animate while data is actually missing.
 */
@Composable
fun ShimmerHost(
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        content()
        return
    }

    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SWEEP_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-progress",
    )
    val state = remember(progress) { ShimmerState(progress) }

    CompositionLocalProvider(LocalShimmer provides state, content = content)
}

/** A grey block that breathes a highlight across itself. */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    val shimmer = LocalShimmer.current
    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    // A faint white sheen reads as "light catching the surface" on any base
    // colour, and stays visible without ever looking like a bright stripe.
    val highlight = Color.White.copy(alpha = 0.07f)

    Box(
        modifier
            .clip(shape)
            .drawBehind {
                drawRect(color = base)

                val progress = shimmer?.progress() ?: return@drawBehind
                val band = size.width * SWEEP_WIDTH
                if (band <= 0f) return@drawBehind

                val x = -band + progress * (size.width + band)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, highlight, Color.Transparent),
                        start = Offset(x, 0f),
                        end = Offset(x + band, 0f),
                    ),
                )
            },
    )
}

@Composable
fun SkeletonLine(
    width: Dp,
    height: Dp = 13.dp,
    modifier: Modifier = Modifier,
) {
    SkeletonBox(
        modifier = modifier.width(width).height(height),
        shape = RoundedCornerShape(height / 2),
    )
}

/** Mirrors a song row: square art, title line, subtitle line. */
@Composable
fun SkeletonTrackRow(
    artSize: Dp = 52.dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBox(Modifier.size(artSize), RoundedCornerShape(10.dp))
        Spacer(Modifier.width(13.dp))
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            SkeletonLine(width = 190.dp)
            SkeletonLine(width = 110.dp, height = 11.dp)
        }
    }
}

/** Mirrors a playlist card in a carousel. */
@Composable
fun SkeletonPlaylistCard(
    size: Dp = 148.dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(size),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        SkeletonBox(Modifier.size(size), RoundedCornerShape(14.dp))
        SkeletonLine(width = size * 0.8f)
        SkeletonLine(width = size * 0.55f, height = 11.dp)
    }
}

/** Mirrors a playlist row in a vertical list. */
@Composable
fun SkeletonPlaylistRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBox(Modifier.size(56.dp), RoundedCornerShape(12.dp))
        Spacer(Modifier.width(13.dp))
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            SkeletonLine(width = 170.dp)
            SkeletonLine(width = 100.dp, height = 11.dp)
        }
    }
}

/** A carousel-sized skeleton, used at the top of Home and Search. */
@Composable
fun SkeletonPlaylistCarousel(
    count: Int = 3,
    cardSize: Dp = 148.dp,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        userScrollEnabled = false,
    ) {
        items(count) {
            SkeletonPlaylistCard(size = cardSize)
        }
    }
}

/**
 * Swaps a skeleton screen for the real content with a soft crossfade.
 *
 * The skeleton mirrors the loaded layout, so nothing shifts: the placeholders
 * simply resolve into artwork and text while the sweep fades away.
 */
@Composable
fun LoadingCrossfade(
    loading: Boolean,
    modifier: Modifier = Modifier,
    skeleton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AnimatedContent(
        targetState = loading,
        modifier = modifier,
        transitionSpec = {
            (fadeIn(animationSpec = tween(durationMillis = 280, delayMillis = 60)) togetherWith
                fadeOut(animationSpec = tween(durationMillis = 160))).using(null)
        },
        label = "loading-crossfade",
    ) { isLoading ->
        if (isLoading) skeleton() else content()
    }
}

/**
 * Thin indeterminate bar for work that happens on top of content that is
 * already on screen (opening a playlist, refreshing a list).
 */
@Composable
fun LoadingBar(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!visible) return

    LinearProgressIndicator(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp),
        color = Violet,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    )
}
