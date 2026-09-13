package com.endroid.code.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Tokyo Night inspired palette for a sleek coding feel
private val DarkPrimary = Color(0xFF7AA2F7)
private val DarkOnPrimary = Color(0xFF1A1B26)
private val DarkSecondary = Color(0xFFBB9AF7)
private val DarkTertiary = Color(0xFF9ECE6A)
private val DarkBackground = Color(0xFF1A1B26)
private val DarkSurface = Color(0xFF24283B)
private val DarkOnBackground = Color(0xFFC0CAF5)
private val DarkOnSurface = Color(0xFFC0CAF5)
private val DarkSurfaceVariant = Color(0xFF414868)
private val DarkOutline = Color(0xFF565F89)

private val LightPrimary = Color(0xFF2E5AAC)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightSecondary = Color(0xFF5C2D91)
private val LightTertiary = Color(0xFF0A7A0A)
private val LightBackground = Color(0xFFF8F9FC)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnBackground = Color(0xFF1A1B26)
private val LightOnSurface = Color(0xFF1A1B26)
private val LightSurfaceVariant = Color(0xFFE8EAF0)
private val LightOutline = Color(0xFF9AA0B4)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    outline = DarkOutline,
    error = Color(0xFFF7768E)
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    outline = LightOutline,
    error = Color(0xFFCF222E)
)

@Composable
fun CodeCanvasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Prefer consistent coding palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
