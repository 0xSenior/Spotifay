package com.example.spotifay.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core palette — original Spotifay identity (violet → cyan, deliberately not Spotify green)
val Violet = Color(0xFF7C5CFF)
val VioletDeep = Color(0xFF5B3FE0)
val BlueAccent = Color(0xFF5B8CFF)
val Cyan = Color(0xFF22D3EE)
val CyanDeep = Color(0xFF0EA5C4)

val Bg = Color(0xFF0B0B12)
val BgElevated = Color(0xFF101019)
val Surface1 = Color(0xFF14141F)
val Surface2 = Color(0xFF1D1D2B)
val Surface3 = Color(0xFF26263A)

val OnAccent = Color(0xFF0B0B12)
val TextPrimary = Color(0xFFF2F2F7)
val TextSecondary = Color(0xFF9BA0B0)
val TextTertiary = Color(0xFF6B7080)

val Outline = Color(0xFF2E2E42)
val OutlineVariant = Color(0xFF1F1F2E)
val ErrorRed = Color(0xFFFF5C76)

val BrandBrush = Brush.linearGradient(listOf(Violet, BlueAccent, Cyan))

/** Deterministic gradient used for placeholder artwork tiles. */
fun artworkBrush(seed: Int): Brush {
    val palettes = listOf(
        listOf(Color(0xFF7C5CFF), Color(0xFF22D3EE)),
        listOf(Color(0xFFFF6B9A), Color(0xFFFFB86B)),
        listOf(Color(0xFF3EE0A0), Color(0xFF22D3EE)),
        listOf(Color(0xFF6B5BFF), Color(0xFFFF6B9A)),
        listOf(Color(0xFF22D3EE), Color(0xFF6B5BFF)),
        listOf(Color(0xFFFFB86B), Color(0xFFFF5C76)),
    )
    val pair = palettes[Math.floorMod(seed, palettes.size)]
    return Brush.linearGradient(pair)
}