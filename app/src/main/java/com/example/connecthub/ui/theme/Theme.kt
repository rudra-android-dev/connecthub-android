package com.example.connecthub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Brand500,
    onPrimary = Neutral100,
    primaryContainer = Brand200,
    onPrimaryContainer = Neutral900,
    secondary = Brand700,
    onSecondary = Neutral100,
    background = Neutral100,
    onBackground = Neutral900,
    surface = Neutral100,
    onSurface = Neutral900,
    surfaceVariant = Neutral200,
    onSurfaceVariant = Neutral800,
    error = Error500,
    onError = Neutral100
)

private val DarkColorScheme = darkColorScheme(
    primary = Brand200,
    onPrimary = Neutral900,
    primaryContainer = Brand700,
    onPrimaryContainer = Neutral100,
    secondary = Brand500,
    onSecondary = Neutral900,
    background = Neutral900,
    onBackground = Neutral100,
    surface = Neutral800,
    onSurface = Neutral100,
    surfaceVariant = Neutral800,
    onSurfaceVariant = Neutral200,
    error = Error500,
    onError = Neutral100
)

@Composable
fun ConnectHubTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}