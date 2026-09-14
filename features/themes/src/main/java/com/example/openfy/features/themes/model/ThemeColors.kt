/*
 * Copyright (C) 2026 ArtiomITPROGRAMING
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
