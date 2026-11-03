package com.example.spotifay.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotifay.ui.theme.BrandBrush

/**
 * The Spotifay mark: a vinyl ring with a play head and an orbiting dot.
 * Drawn in code so the in-app identity matches the launcher icon.
 */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    Box(modifier = modifier.size(size)) {
        Canvas(Modifier.matchParentSize()) {
            val d = this.size.minDimension
            val stroke = d * 0.11f
            val radius = d / 2f - stroke / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            drawCircle(
                brush = BrandBrush,
                radius = radius,
                center = center,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )

            val triangle = Path().apply {
                moveTo(center.x - 0.070f * d, center.y - 0.130f * d)
                lineTo(center.x + 0.150f * d, center.y)
                lineTo(center.x - 0.070f * d, center.y + 0.130f * d)
                close()
            }
            drawPath(triangle, color = androidx.compose.ui.graphics.Color.White)

            drawCircle(
                color = androidx.compose.ui.graphics.Color(0xFF22D3EE),
                radius = d * 0.032f,
                center = Offset(center.x, center.y - 0.365f * d),
            )
        }
    }
}

@Composable
fun SpotifayWordmark(
    modifier: Modifier = Modifier,
    markSize: Dp = 28.dp,
    textSize: Int = 20,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        BrandMark(size = markSize)
        Spacer(Modifier.width(9.dp))
        Text(
            text = "Spotifay",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = textSize.sp,
                letterSpacing = (-0.3).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}