package se.payerl.mobilenotes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Application color palette - internal colors used by the theme
 */
private object Colors {
    val GreenPrimary = Color(0xFF4CAF50)
    val GreenPrimaryVariant = Color(0xFF388E3C)
    val BackgroundLight = Color(0xFFE8F5E9)
    val White = Color.White
    val Black = Color.Black
    val ErrorRed = Color(0xFFB00020)
}

/**
 * Light color scheme for the app
 */
private val LightColorScheme = lightColorScheme(
    primary = Colors.GreenPrimary,
    onPrimary = Colors.White,
    primaryContainer = Colors.GreenPrimaryVariant,
    onPrimaryContainer = Colors.White,
    secondary = Colors.GreenPrimaryVariant,
    onSecondary = Colors.White,
    background = Colors.BackgroundLight,
    onBackground = Colors.Black,
    surface = Colors.White,
    onSurface = Colors.Black,
    error = Colors.ErrorRed,
    onError = Colors.White
)

/**
 * Dark color scheme for the app
 */
private val DarkColorScheme = darkColorScheme(
    primary = Colors.GreenPrimary,
    onPrimary = Colors.White,
    primaryContainer = Colors.GreenPrimaryVariant,
    onPrimaryContainer = Colors.White,
    secondary = Colors.GreenPrimaryVariant,
    onSecondary = Colors.White,
    background = Color(0xFF121212),
    onBackground = Colors.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Colors.White,
    error = Colors.ErrorRed,
    onError = Colors.White
)

/**
 * MobileNotes app theme that works on all platforms (Android, iOS, Desktop, Web)
 *
 * Usage:
 * ```
 * MobileNotesTheme {
 *     // Your composable content here
 * }
 * ```
 *
 * @param darkTheme Whether to use dark theme. Defaults to system preference.
 * @param content The composable content to apply the theme to.
 */
@Composable
fun MobileNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

