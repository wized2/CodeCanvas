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

private val DarkPrimary = Color(0xFF7AA2F7)
private val DarkOnPrimary = Color(0xFF1A1B26)
private val DarkSecondary = Color(0xFFBB9AF7)
private val DarkOnSecondary = Color(0xFF1A1B26)
private val DarkTertiary = Color(0xFF9ECE6A)
private val DarkOnTertiary = Color(0xFF1A1B26)
private val DarkBackground = Color(0xFF1A1B26)
private val DarkSurface = Color(0xFF1A1B26)
private val DarkSurfaceContainer = Color(0xFF24283B)
private val DarkSurfaceContainerHigh = Color(0xFF2A2E42)
private val DarkOnBackground = Color(0xFFC0CAF5)
private val DarkOnSurface = Color(0xFFC0CAF5)
private val DarkSurfaceVariant = Color(0xFF414868)
private val DarkOnSurfaceVariant = Color(0xFFA9B1D6)
private val DarkOutline = Color(0xFF565F89)
private val DarkOutlineVariant = Color(0xFF3B4261)

private val LightPrimary = Color(0xFF2E5AAC)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightSecondary = Color(0xFF5C2D91)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightTertiary = Color(0xFF0A7A0A)
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightBackground = Color(0xFFF8F9FC)
private val LightSurface = Color(0xFFF8F9FC)
private val LightSurfaceContainer = Color(0xFFFFFFFF)
private val LightSurfaceContainerHigh = Color(0xFFEEF0F6)
private val LightOnBackground = Color(0xFF1A1B26)
private val LightOnSurface = Color(0xFF1A1B26)
private val LightSurfaceVariant = Color(0xFFE8EAF0)
private val LightOnSurfaceVariant = Color(0xFF444A5C)
private val LightOutline = Color(0xFF9AA0B4)
private val LightOutlineVariant = Color(0xFFCACDD9)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerLowest = Color(0xFF16161E),
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Color(0xFFF7768E),
    onError = Color(0xFF1A1B26),
    primaryContainer = Color(0xFF3D59A1),
    onPrimaryContainer = Color(0xFFC0CAF5),
    secondaryContainer = Color(0xFF565F89),
    onSecondaryContainer = Color(0xFFC0CAF5),
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    background = LightBackground,
    surface = LightSurface,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = Color(0xFFCF222E),
    onError = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondaryContainer = Color(0xFFEEDCFF),
    onSecondaryContainer = Color(0xFF2A0053),
)

@Composable
fun CodeCanvasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
            val insets = WindowCompat.getInsetsController(window, view)
            insets.isAppearanceLightStatusBars = !darkTheme
            insets.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
