package com.gamehub.wordgame.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun WordGameTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val wg = if (darkTheme) DarkWgColors else LightWgColors
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = wg.accent, background = wg.surfacePage, surface = wg.surfaceCard,
            onBackground = wg.textStrong, onSurface = wg.textStrong, error = wg.stateError
        )
    } else {
        lightColorScheme(
            primary = wg.accent, background = wg.surfacePage, surface = wg.surfaceCard,
            onBackground = wg.textStrong, onSurface = wg.textStrong, error = wg.stateError
        )
    }
    CompositionLocalProvider(LocalWgColors provides wg) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
