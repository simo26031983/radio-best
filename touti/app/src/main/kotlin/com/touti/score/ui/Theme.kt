package com.touti.score.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.touti.score.model.Team

val MarocGreen = Color(0xFF0B7A3E)
val MarocRed = Color(0xFFC1272D)
val Gold = Color(0xFFF4B400)

fun teamColor(team: Team): Color = if (team == Team.A) MarocGreen else MarocRed

private val LightColors = lightColorScheme(
    primary = MarocRed,
    onPrimary = Color.White,
    secondary = MarocGreen,
    onSecondary = Color.White,
    tertiary = Gold,
    background = Color(0xFFFFF8EC),
    surface = Color(0xFFFFF8EC),
    surfaceContainer = Color(0xFFF7ECD8),
    surfaceContainerHigh = Color(0xFFF2E4CC),
    surfaceContainerLow = Color(0xFFFFFFFF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF6B6B),
    onPrimary = Color(0xFF3B0000),
    secondary = Color(0xFF4CC38A),
    tertiary = Gold,
    background = Color(0xFF15110C),
    surface = Color(0xFF15110C),
    surfaceContainer = Color(0xFF241D15),
    surfaceContainerHigh = Color(0xFF2E251B),
    surfaceContainerLow = Color(0xFF1C1610),
)

@Composable
fun ToutiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
