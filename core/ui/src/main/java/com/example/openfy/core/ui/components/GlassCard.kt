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

package com.example.openfy.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.openfy.core.ui.theme.GlassBorderStroke
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.glowBorder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = GlassDarkSurface,
    borderColor: Color = GlassBorderStroke,
    borderWidth: Dp = 1.dp,
    hasGlowBorder: Boolean = false,
    glowColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            onClick = { onClick?.invoke() },
            onLongClick = { onLongClick?.invoke() }
        )
    } else Modifier

    val glowModifier = if (hasGlowBorder && glowColor != Color.Unspecified) {
        Modifier.glowBorder(glowColor = glowColor, shape = shape)
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .then(glowModifier)
            .then(clickModifier),
        shape = shape,
        color = backgroundColor.copy(alpha = 0.65f),
        border = if (!hasGlowBorder) {
            BorderStroke(
                borderWidth,
                Brush.linearGradient(
                    listOf(
                        borderColor.copy(alpha = 0.6f),
                        borderColor.copy(alpha = 0.15f),
                        borderColor.copy(alpha = 0.4f)
                    )
                )
            )
        } else null,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Box(content = content)
    }
}
