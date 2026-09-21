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

package com.example.openfy.ui.mood

import androidx.compose.ui.graphics.Color
import com.example.openfy.core.audio.model.Song
import java.util.Locale
import kotlin.math.abs
import kotlin.math.hypot

/**
 * 2D Mood Coordinates:
 * - valence: -1.0f (Dark / Melancholic) to +1.0f (Bright / Euphoric)
 * - energy: -1.0f (Calm / Ambient) to +1.0f (Intense / Driving)
 */
data class MoodPoint(
    val valence: Float,
    val energy: Float
)

enum class MoodQuadrant(
    val title: String,
    val description: String,
    val accentColor: Color,
    val gradientColors: List<Color>
) {
    DRIVE_EUPHORIA(
        title = "Драйв & Эйфория",
        description = "Танцевальный пульс, позитив и яркая энергия",
        accentColor = Color(0xFFFF9100), // Amber Gold
        gradientColors = listOf(Color(0xFFFF9100), Color(0xFFFF0055))
    ),
    REBEL_AGGRESSION(
        title = "Бунт & Агрессия",
        description = "Тяжёлый бас, драйв, ночная тьма и бунт",
        accentColor = Color(0xFFFF0055), // Crimson Pink
        gradientColors = listOf(Color(0xFFFF0055), Color(0xFF7000FF))
    ),
    MELANCHOLY_NOSTALGIA(
        title = "Меланхолия & Ностальгия",
        description = "Тихая грусть, акустика, эмбиент и воспоминания",
        accentColor = Color(0xFF00E5FF), // Cyan
        gradientColors = listOf(Color(0xFF00E5FF), Color(0xFF1A237E))
    ),
    WARMTH_CHILL(
        title = "Тепло & Чилл",
        description = "Уютный лаунж, мягкий джаз, соул и спокойствие",
        accentColor = Color(0xFF00E676), // Emerald Mint
        gradientColors = listOf(Color(0xFF00E676), Color(0xFF00B0FF))
    );

    companion object {
        fun fromCoordinates(valence: Float, energy: Float): MoodQuadrant {
            return when {
                valence >= 0f && energy >= 0f -> DRIVE_EUPHORIA
                valence < 0f && energy >= 0f -> REBEL_AGGRESSION
                valence < 0f && energy < 0f -> MELANCHOLY_NOSTALGIA
                else -> WARMTH_CHILL
            }
        }
    }
}

object MoodClassifier {

    /**
     * Classifies a song into a 2D (valence, energy) coordinate using genre,
     * title, artist, duration heuristics and deterministic dispersion.
     */
    fun classifySong(song: Song): MoodPoint {
        val titleLower = song.title.lowercase(Locale.ROOT)
        val artistLower = song.artist.lowercase(Locale.ROOT)
        val albumLower = song.album.lowercase(Locale.ROOT)
        val pathLower = song.path.lowercase(Locale.ROOT)
        val combined = "$titleLower $artistLower $albumLower $pathLower"

        var valence = 0f
        var energy = 0f

        // 1. High Energy & Dark Keywords
        if (combined.contains("metal") || combined.contains("rock") || combined.contains("phonk") ||
            combined.contains("drill") || combined.contains("trap") || combined.contains("dark") ||
            combined.contains("rage") || combined.contains("fight") || combined.contains("blood")
        ) {
            energy += 0.55f
            valence -= 0.45f
        }

        // 2. High Energy & Bright Keywords
        if (combined.contains("pop") || combined.contains("dance") || combined.contains("edm") ||
            combined.contains("party") || combined.contains("summer") || combined.contains("disco") ||
            combined.contains("happy") || combined.contains("sun") || combined.contains("joy")
        ) {
            energy += 0.50f
            valence += 0.55f
        }

        // 3. Low Energy & Dark / Melancholic Keywords
        if (combined.contains("sad") || combined.contains("tears") || combined.contains("cry") ||
            combined.contains("rain") || combined.contains("lonely") || combined.contains("night") ||
            combined.contains("slow") || combined.contains("ballad") || combined.contains("acoustic")
        ) {
            energy -= 0.45f
            valence -= 0.40f
        }

        // 4. Low Energy & Warm / Chill Keywords
        if (combined.contains("chill") || combined.contains("relax") || combined.contains("jazz") ||
            combined.contains("soul") || combined.contains("lofi") || combined.contains("ambient") ||
            combined.contains("lounge") || combined.contains("calm") || combined.contains("breeze")
        ) {
            energy -= 0.50f
            valence += 0.40f
        }

        // 5. Deterministic subtle dispersion based on song ID and title hash
        val hash = (song.id.hashCode() xor song.title.hashCode())
        val hashValenceOffset = ((hash % 100).toFloat() / 250f) // -0.4f..+0.4f
        val hashEnergyOffset = (((hash / 100) % 100).toFloat() / 250f)

        valence = (valence + hashValenceOffset).coerceIn(-1.0f, 1.0f)
        energy = (energy + hashEnergyOffset).coerceIn(-1.0f, 1.0f)

        return MoodPoint(valence, energy)
    }

    /**
     * Finds and sorts songs in the library that match the selected target (valence, energy)
     * ordered by Euclidean proximity.
     */
    fun findMatchingSongs(
        allSongs: List<Song>,
        targetValence: Float,
        targetEnergy: Float,
        limit: Int = 30
    ): List<Song> {
        if (allSongs.isEmpty()) return emptyList()

        return allSongs
            .map { song ->
                val point = classifySong(song)
                val dist = hypot(point.valence - targetValence, point.energy - targetEnergy)
                song to dist
            }
            .sortedBy { it.second }
            .take(limit)
            .map { it.first }
    }
}
