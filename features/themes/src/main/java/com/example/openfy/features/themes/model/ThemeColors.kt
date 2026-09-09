package com.example.openfy.features.themes.model

import kotlinx.serialization.Serializable

@Serializable
data class ThemeColors(
    val primary: String,
    val onPrimary: String = "#FFFFFF",
    val primaryContainer: String? = null,
    val onPrimaryContainer: String? = null,
    val secondary: String? = null,
    val onSecondary: String = "#FFFFFF",
    val secondaryContainer: String? = null,
    val onSecondaryContainer: String? = null,
    val tertiary: String? = null,
    val onTertiary: String = "#FFFFFF",
    val tertiaryContainer: String? = null,
    val onTertiaryContainer: String? = null,
    val background: String = "#0A0A0E",
    val onBackground: String = "#EDEDF2",
    val surface: String = "#121218",
    val onSurface: String = "#EDEDF2",
    val surfaceVariant: String? = null,
    val onSurfaceVariant: String = "#8E8E98",
    val outline: String = "#26262E",
    val outlineVariant: String? = null,
    val accent: String? = null,
    val cardColor: String? = null
)
