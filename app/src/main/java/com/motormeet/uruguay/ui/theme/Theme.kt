package com.motormeet.uruguay.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ColorWhite = Color.White

private val DarkColorScheme = darkColorScheme(
    primary = MotorRed,
    onPrimary = ColorWhite,
    secondary = MotorBlue,
    onSecondary = ColorWhite,
    background = MotorBlack,
    onBackground = MotorText,
    surface = MotorSurface,
    onSurface = MotorText,
    surfaceVariant = MotorSurface2,
    onSurfaceVariant = MotorMuted,
    outline = MotorLine,
    error = Color(0xFFFF5969)
)

private val LightColorScheme = lightColorScheme(
    primary = MotorRedDark,
    onPrimary = ColorWhite,
    secondary = MotorBlue,
    onSecondary = ColorWhite,
    background = LightSurface,
    onBackground = OnLightSurface,
    surface = Color.White,
    onSurface = OnLightSurface,
    surfaceVariant = Color(0xFFE9EBEF),
    onSurfaceVariant = Color(0xFF5E6570),
    outline = Color(0xFFD2D6DC),
    error = Color(0xFFB3261E)
)

@Composable
fun MotorMeetTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
