package com.example.openfy.features.themes.model

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Parses a hex string in format "#RRGGBB", "#AARRGGBB", "RRGGBB", or "AARRGGBB" into a Compose [Color].
 * Returns fallback color if hex format is invalid.
 */
fun parseHexColor(hex: String?, fallback: Color = Color.White): Color {
    if (hex.isNullOrBlank()) return fallback
    val cleanHex = hex.trim().removePrefix("#").removePrefix("0x")
    return try {
        when (cleanHex.length) {
            6 -> {
                val rgb = cleanHex.toLong(16)
                Color(0xFF000000 or rgb)
            }
            8 -> {
                val argb = cleanHex.toLong(16)
                Color(argb)
            }
            3 -> { // #RGB -> #RRGGBB
                val r = cleanHex[0]
                val g = cleanHex[1]
                val b = cleanHex[2]
                val expanded = "$r$r$g$g$b$b"
                Color(0xFF000000 or expanded.toLong(16))
            }
            4 -> { // #ARGB -> #AARRGGBB
                val a = cleanHex[0]
                val r = cleanHex[1]
                val g = cleanHex[2]
                val b = cleanHex[3]
                val expanded = "$a$a$r$r$g$g$b$b"
                Color(expanded.toLong(16))
            }
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

/**
 * Converts [ThemeColors] into Material 3 [ColorScheme].
 */
fun ThemeColors.toColorScheme(isDark: Boolean = true): ColorScheme {
    val primaryColor = parseHexColor(primary, fallback = if (isDark) Color(0xFF00E5FF) else Color(0xFF6750A4))
    val onPrimaryColor = parseHexColor(onPrimary, fallback = if (isDark) Color(0xFF00363D) else Color.White)
    val primContainer = parseHexColor(primaryContainer, fallback = primaryColor.copy(alpha = 0.25f))
    val onPrimContainer = parseHexColor(onPrimaryContainer, fallback = primaryColor)

    val secondaryColor = parseHexColor(secondary ?: accent, fallback = if (isDark) Color(0xFFBD00FF) else Color(0xFF625B71))
    val onSecondaryColor = parseHexColor(onSecondary, fallback = Color.White)
    val secContainer = parseHexColor(secondaryContainer, fallback = secondaryColor.copy(alpha = 0.2f))
    val onSecContainer = parseHexColor(onSecondaryContainer, fallback = secondaryColor)

    val tertiaryColor = parseHexColor(tertiary, fallback = if (isDark) Color(0xFFFF0055) else Color(0xFF7D5260))
    val onTertiaryColor = parseHexColor(onTertiary, fallback = Color.White)
    val tertContainer = parseHexColor(tertiaryContainer, fallback = tertiaryColor.copy(alpha = 0.2f))
    val onTertContainer = parseHexColor(onTertiaryContainer, fallback = tertiaryColor)

    val bgColor = parseHexColor(background, fallback = if (isDark) Color(0xFF0A0A0E) else Color(0xFFFEF7FF))
    val onBgColor = parseHexColor(onBackground, fallback = if (isDark) Color(0xFFEDEDF2) else Color(0xFF1D1B20))

    val surfColor = parseHexColor(cardColor ?: surface, fallback = if (isDark) Color(0xFF121218) else Color(0xFFFEF7FF))
    val onSurfColor = parseHexColor(onSurface, fallback = if (isDark) Color(0xFFEDEDF2) else Color(0xFF1D1B20))
    val surfVarColor = parseHexColor(surfaceVariant, fallback = if (isDark) Color(0xFF1A1A22) else Color(0xFFE7E0EC))
    val onSurfVarColor = parseHexColor(onSurfaceVariant, fallback = if (isDark) Color(0xFF8E8E98) else Color(0xFF49454F))

    val outlineColor = parseHexColor(outline, fallback = if (isDark) Color(0xFF26262E) else Color(0xFF79747E))
    val outlineVarColor = parseHexColor(outlineVariant, fallback = if (isDark) Color(0xFF18181D) else Color(0xFFCAC4D0))

    return if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = onPrimaryColor,
            primaryContainer = primContainer,
            onPrimaryContainer = onPrimContainer,
            secondary = secondaryColor,
            onSecondary = onSecondaryColor,
            secondaryContainer = secContainer,
            onSecondaryContainer = onSecContainer,
            tertiary = tertiaryColor,
            onTertiary = onTertiaryColor,
            tertiaryContainer = tertContainer,
            onTertiaryContainer = onTertContainer,
            background = bgColor,
            onBackground = onBgColor,
            surface = surfColor,
            onSurface = onSurfColor,
            surfaceVariant = surfVarColor,
            onSurfaceVariant = onSurfVarColor,
            outline = outlineColor,
            outlineVariant = outlineVarColor
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = onPrimaryColor,
            primaryContainer = primContainer,
            onPrimaryContainer = onPrimContainer,
            secondary = secondaryColor,
            onSecondary = onSecondaryColor,
            secondaryContainer = secContainer,
            onSecondaryContainer = onSecContainer,
            tertiary = tertiaryColor,
            onTertiary = onTertiaryColor,
            tertiaryContainer = tertContainer,
            onTertiaryContainer = onTertContainer,
            background = bgColor,
            onBackground = onBgColor,
            surface = surfColor,
            onSurface = onSurfColor,
            surfaceVariant = surfVarColor,
            onSurfaceVariant = onSurfVarColor,
            outline = outlineColor,
            outlineVariant = outlineVarColor
        )
    }
}

/**
 * Converts [ThemeColorsConfig] into Material 3 [ColorScheme].
 */
fun ThemeColorsConfig.toColorScheme(isDark: Boolean = true): ColorScheme {
    val themeColors = ThemeColors(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant,
        accent = accent,
        cardColor = cardColor
    )
    return themeColors.toColorScheme(isDark)
}
