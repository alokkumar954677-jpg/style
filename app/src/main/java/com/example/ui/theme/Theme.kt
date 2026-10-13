package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StudioColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = LuxuryBlack,
    primaryContainer = PurpleDark,
    onPrimaryContainer = PurpleLight,
    secondary = PurpleAccent,
    onSecondary = LuxuryBlack,
    secondaryContainer = LuxuryCardSurface,
    onSecondaryContainer = PurpleLight,
    tertiary = ChampagneGold,
    onTertiary = LuxuryBlack,
    background = LuxuryBlack,
    onBackground = TextPrimary,
    surface = LuxuryDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = LuxuryCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = LuxuryCardBorder,
    outlineVariant = PurpleDark,
    error = ErrorRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to stunning luxury dark aesthetic
    dynamicColor: Boolean = false, // Keep signature purple & black studio identity
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = LuxuryBlack.toArgb()
                window.navigationBarColor = LuxuryBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}
