package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MunnaColorScheme = darkColorScheme(
    primary = MunnaSkyBlue,
    onPrimary = MunnaNavyDeep,
    primaryContainer = MunnaSkyBlueDeep,
    onPrimaryContainer = MunnaTextWhite,
    secondary = MunnaCyanGlow,
    onSecondary = MunnaNavyDeep,
    secondaryContainer = MunnaCardElevated,
    onSecondaryContainer = MunnaCyanSoft,
    tertiary = MunnaSuccessGreen,
    onTertiary = MunnaNavyDeep,
    background = MunnaNavyDeep,
    onBackground = MunnaTextWhite,
    surface = MunnaCardNavy,
    onSurface = MunnaTextWhite,
    surfaceVariant = MunnaCardElevated,
    onSurfaceVariant = MunnaTextSecondary,
    outline = MunnaCardBorder,
    error = MunnaErrorRed,
    onError = MunnaTextWhite,
    errorContainer = MunnaErrorBg,
    onErrorContainer = MunnaErrorRed
)

val MunnaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MunnaAITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MunnaColorScheme,
        typography = Typography,
        shapes = MunnaShapes,
        content = content
    )
}
