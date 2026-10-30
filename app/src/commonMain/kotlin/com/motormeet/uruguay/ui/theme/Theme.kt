package com.motormeet.uruguay.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
