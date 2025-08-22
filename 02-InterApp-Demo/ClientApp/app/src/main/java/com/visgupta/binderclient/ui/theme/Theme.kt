package com.visgupta.binderclient.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ClientColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    secondary = Color(0xFFFF6F00),
    tertiary = Color(0xFF6A1B9A),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun BinderClientTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ClientColorScheme,
        typography = Typography(),
        content = content
    )
}
