package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GeometricColorScheme = lightColorScheme(
    primary = Indigo600,
    secondary = Emerald500,
    tertiary = Orange500,
    background = GeoBackground,
    surface = GeoSurface,
    onPrimary = GeoSurface,
    onSecondary = GeoSurface,
    onTertiary = GeoSurface,
    onBackground = Slate900,
    onSurface = Slate900,
    outline = GeoBorder,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Keep warm light theme of Geometric Balance by default
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce the geometric theme design
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = GeometricColorScheme,
        typography = Typography,
        content = content
    )
}
