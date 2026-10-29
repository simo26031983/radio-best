package com.touti.score.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

private class Piece(
    val x: Float,
    val delay: Float,
    val speed: Float,
    val sway: Float,
    val spin: Float,
    val size: Float,
    val color: Color,
)

private val ConfettiColors = listOf(MarocRed, MarocGreen, Gold, Color.White, Color(0xFF2E86FF))

/** Endless confetti rain, positions in fractions of the canvas so it fits any screen. */
@Composable
fun Confetti(modifier: Modifier = Modifier) {
    val pieces = remember {
        List(140) {
            Piece(
                x = Random.nextFloat(),
                delay = Random.nextFloat(),
                speed = 0.18f + Random.nextFloat() * 0.25f,
                sway = Random.nextFloat() * 6.28f,
                spin = 90f + Random.nextFloat() * 360f,
                size = 8f + Random.nextFloat() * 14f,
                color = ConfettiColors.random(),
            )
        }
    }
    var elapsedNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { elapsedNanos = it - start }
        }
    }
    Canvas(modifier) {
        val t = elapsedNanos / 1_000_000_000f
        pieces.forEach { p ->
            // Each piece loops from just above the top to just below the bottom.
            val progress = ((t * p.speed) + p.delay) % 1.2f - 0.1f
            val y = progress * size.height
            val x = (p.x + 0.03f * sin(t * 2f + p.sway)) * size.width
            rotate(degrees = t * p.spin, pivot = Offset(x, y)) {
                drawRect(p.color, topLeft = Offset(x - p.size / 2, y - p.size / 4), size = Size(p.size, p.size / 2))
            }
        }
    }
}
