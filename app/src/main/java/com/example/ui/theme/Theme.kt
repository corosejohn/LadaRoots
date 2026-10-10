package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = Color(0xFF003822),
    primaryContainer = ForestGreenLight,
    onPrimaryContainer = Color(0xFFD8F3DC),
    secondary = LeafGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E3A2F),
    onSecondaryContainer = Color(0xFFD8F3DC),
    tertiary = HarvestGold,
    onTertiary = Color.Black,
    background = DarkGreenBackground,
    onBackground = Color(0xFFE2E3DE),
    surface = DarkGreenSurface,
    onSurface = Color(0xFFE2E3DE),
    surfaceVariant = DarkGreenSurfaceVariant,
    onSurfaceVariant = Color(0xFFC2C8BE)
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = SoftMint,
    onPrimaryContainer = ForestGreenPrimary,
    secondary = LeafGreen,
    onSecondary = Color.White,
    secondaryContainer = MintSurface,
    onSecondaryContainer = ForestGreenLight,
    tertiary = HarvestGold,
    onTertiary = Color.White,
    tertiaryContainer = HarvestGoldLight,
    onTertiaryContainer = Color(0xFF78350F),
    background = WarmCreamBackground,
    onBackground = Color(0xFF1B201D),
    surface = WarmSurface,
    onSurface = Color(0xFF1B201D),
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = Color(0xFF434942),
    outline = Color(0xFF747970),
    outlineVariant = Color(0xFFC4C8BE)
)

@Composable
fun LadaRootsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded forest green by default
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
