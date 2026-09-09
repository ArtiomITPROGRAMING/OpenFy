package com.example.openfy.core.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.openfy.core.audio.data.AppThemeStyle

val SeriousDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF26262C),
    onPrimaryContainer = Color(0xFFFFFFFF),

    secondary = Color(0xFFB0B0B8),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF1A1A20),
    onSecondaryContainer = Color(0xFFE2E2E8),

    tertiary = Color(0xFF7E7E88),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF141418),
    onTertiaryContainer = Color(0xFFCACACE),

    background = Color(0xFF000000),
    onBackground = Color(0xFFEDEDF2),

    surface = Color(0xFF0A0A0D),
    onSurface = Color(0xFFEDEDF2),
    surfaceVariant = Color(0xFF141418),
    onSurfaceVariant = Color(0xFF8E8E98),

    outline = Color(0xFF26262E),
    outlineVariant = Color(0xFF18181D)
)

val MidnightNeonColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),

    secondary = ElectricPurple,
    onSecondary = Color(0xFF38006B),
    secondaryContainer = Color(0xFF55009E),
    onSecondaryContainer = Color(0xFFEADBFF),

    tertiary = NeonPink,
    onTertiary = Color(0xFF5C0028),
    tertiaryContainer = Color(0xFF82003D),
    onTertiaryContainer = Color(0xFFFFD9E2),

    background = DarkBackground,
    onBackground = Color(0xFFE3E2E6),

    surface = DarkSurface,
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC4C6D0),

    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F)
)

val CyberpunkColorScheme = darkColorScheme(
    primary = CyberpunkRubyRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF520616),
    onPrimaryContainer = Color(0xFFFFD9DF),

    secondary = CyberpunkGold,
    onSecondary = Color(0xFF432C00),
    secondaryContainer = Color(0xFF604100),
    onSecondaryContainer = Color(0xFFFFDF9E),

    tertiary = NeonPink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF82003D),
    onTertiaryContainer = Color(0xFFFFD9E2),

    background = CyberpunkDarkBg,
    onBackground = Color(0xFFFBECEF),

    surface = CyberpunkSurface,
    onSurface = Color(0xFFFBECEF),
    surfaceVariant = CyberpunkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD4C2C7),

    outline = CyberpunkBorder,
    outlineVariant = Color(0xFF360C17)
)

val RetroPixelColorScheme = darkColorScheme(
    primary = RetroPhosphorGreen,
    onPrimary = Color(0xFF050B08),
    primaryContainer = Color(0xFF0B331A),
    onPrimaryContainer = RetroPhosphorGreen,

    secondary = RetroAmber,
    onSecondary = Color(0xFF050B08),
    secondaryContainer = Color(0xFF3D2A00),
    onSecondaryContainer = Color(0xFFFFE082),

    tertiary = Color(0xFF66FF99),
    onTertiary = Color(0xFF050B08),
    tertiaryContainer = Color(0xFF134226),
    onTertiaryContainer = Color(0xFFB9F6CA),

    background = RetroDarkBg,
    onBackground = Color(0xFFE0F2E9),

    surface = RetroSurface,
    onSurface = Color(0xFFE0F2E9),
    surfaceVariant = RetroSurfaceVariant,
    onSurfaceVariant = Color(0xFFA5D6A7),

    outline = RetroBorder,
    outlineVariant = Color(0xFF11261B)
)

private val LightColorScheme = lightColorScheme(
    primary = DeepViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADBFF),
    onPrimaryContainer = Color(0xFF280056),

    secondary = CoralOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF380D00),

    tertiary = MintGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB8F5D8),
    onTertiaryContainer = Color(0xFF002113),

    background = LightBackground,
    onBackground = Color(0xFF191C1E),

    surface = LightSurface,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF44474F),

    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C7D0)
)

@Composable
fun ColorScheme.animated(): ColorScheme {
    val animDuration = 450
    val primaryAnim by animateColorAsState(primary, tween(animDuration), label = "primary")
    val onPrimaryAnim by animateColorAsState(onPrimary, tween(animDuration), label = "onPrimary")
    val primaryContainerAnim by animateColorAsState(primaryContainer, tween(animDuration), label = "primaryContainer")
    val onPrimaryContainerAnim by animateColorAsState(onPrimaryContainer, tween(animDuration), label = "onPrimaryContainer")
    val secondaryAnim by animateColorAsState(secondary, tween(animDuration), label = "secondary")
    val onSecondaryAnim by animateColorAsState(onSecondary, tween(animDuration), label = "onSecondary")
    val secondaryContainerAnim by animateColorAsState(secondaryContainer, tween(animDuration), label = "secondaryContainer")
    val onSecondaryContainerAnim by animateColorAsState(onSecondaryContainer, tween(animDuration), label = "onSecondaryContainer")
    val tertiaryAnim by animateColorAsState(tertiary, tween(animDuration), label = "tertiary")
    val onTertiaryAnim by animateColorAsState(onTertiary, tween(animDuration), label = "onTertiary")
    val tertiaryContainerAnim by animateColorAsState(tertiaryContainer, tween(animDuration), label = "tertiaryContainer")
    val onTertiaryContainerAnim by animateColorAsState(onTertiaryContainer, tween(animDuration), label = "onTertiaryContainer")
    val backgroundAnim by animateColorAsState(background, tween(animDuration), label = "background")
    val onBackgroundAnim by animateColorAsState(onBackground, tween(animDuration), label = "onBackground")
    val surfaceAnim by animateColorAsState(surface, tween(animDuration), label = "surface")
    val onSurfaceAnim by animateColorAsState(onSurface, tween(animDuration), label = "onSurface")
    val surfaceVariantAnim by animateColorAsState(surfaceVariant, tween(animDuration), label = "surfaceVariant")
    val onSurfaceVariantAnim by animateColorAsState(onSurfaceVariant, tween(animDuration), label = "onSurfaceVariant")
    val outlineAnim by animateColorAsState(outline, tween(animDuration), label = "outline")
    val outlineVariantAnim by animateColorAsState(outlineVariant, tween(animDuration), label = "outlineVariant")

    return this.copy(
        primary = primaryAnim,
        onPrimary = onPrimaryAnim,
        primaryContainer = primaryContainerAnim,
        onPrimaryContainer = onPrimaryContainerAnim,
        secondary = secondaryAnim,
        onSecondary = onSecondaryAnim,
        secondaryContainer = secondaryContainerAnim,
        onSecondaryContainer = onSecondaryContainerAnim,
        tertiary = tertiaryAnim,
        onTertiary = onTertiaryAnim,
        tertiaryContainer = tertiaryContainerAnim,
        onTertiaryContainer = onTertiaryContainerAnim,
        background = backgroundAnim,
        onBackground = onBackgroundAnim,
        surface = surfaceAnim,
        onSurface = onSurfaceAnim,
        surfaceVariant = surfaceVariantAnim,
        onSurfaceVariant = onSurfaceVariantAnim,
        outline = outlineAnim,
        outlineVariant = outlineVariantAnim
    )
}

@Composable
fun OpenFyTheme(
    themeStyle: AppThemeStyle = AppThemeStyle.SERIOUS_DARK,
    customColorScheme: ColorScheme? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    customAccentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val baseScheme = customColorScheme ?: when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> SeriousDarkColorScheme
        AppThemeStyle.MIDNIGHT_NEON -> MidnightNeonColorScheme
        AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkColorScheme
        AppThemeStyle.RETRO_PIXEL -> RetroPixelColorScheme
        AppThemeStyle.MATERIAL_YOU -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) MidnightNeonColorScheme else LightColorScheme
            }
        }
    }

    val finalColorScheme = if (customAccentColor != null && themeStyle != AppThemeStyle.SERIOUS_DARK && customColorScheme == null) {
        baseScheme.copy(
            primary = customAccentColor,
            primaryContainer = customAccentColor.copy(alpha = 0.3f)
        )
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = finalColorScheme.animated(),
        typography = Typography,
        content = content
    )
}
