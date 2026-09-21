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

        // Default base is warm and pleasant
        var valence = 0.20f
        var energy = 0.05f
        var matchedAggressive = false

        // 1. High Energy & Aggressive / Brutal / Heavy Metal / Phonk / Drill
        // Strictly requires explicit markers of heavy, aggressive or dark sound
        val aggressiveKeywords = listOf(
            "metal", "deathcore", "death metal", "black metal", "thrash", "hardcore",
            "phonk", "drill", "dark trap", "rage", "blood", "death", "kill", "scream",
            "brutal", "war", "battle", "fight", "hate", "fury", "demon", "hell",
            "doom", "destruction", "aggro", "industrial", "hard rock", "heavy rock",
            "ярость", "метал", "фонк", "битва", "кровь", "смерть", "бунт"
        )
        if (aggressiveKeywords.any { combined.contains(it) }) {
            energy += 0.60f
            valence -= 0.65f
            matchedAggressive = true
        }

        // 2. Romantic, Melodic, Vocal, Love & Soul (e.g. "Did I Tell You")
        val melodicLoveKeywords = listOf(
            "tell", "you", "did", "love", "heart", "baby", "girl", "boy", "feel",
            "kiss", "dream", "sweet", "smile", "forever", "together", "mine", "care",
            "hold", "eyes", "life", "touch", "angel", "darling", "honey", "dear",
            "miss", "remember", "wish", "true", "passion", "romance", "acoustic",
            "piano", "guitar", "ballad", "vocal", "soul", "r&b",
            "любовь", "сердце", "нежность", "милая", "вместе", "душа", "тебя", "меня"
        )
        if (melodicLoveKeywords.any { combined.contains(it) }) {
            valence += 0.40f
            energy -= 0.15f
        }

        // 3. High Energy & Bright / Pop / Dance / EDM
        val brightKeywords = listOf(
            "pop", "dance", "edm", "party", "summer", "disco", "happy", "sun", "joy",
            "shine", "fun", "celebrate", "holiday", "club", "upbeat", "good vibes",
            "electronic", "house", "electro", "лето", "танцы", "праздник", "радость", "солнце"
        )
        if (brightKeywords.any { combined.contains(it) }) {
            energy += 0.50f
            valence += 0.45f
        }

        // 4. Low Energy & Sad / Melancholic / Ballad
        val melancholicKeywords = listOf(
            "sad", "tears", "cry", "crying", "rain", "lonely", "alone", "night",
            "slow", "ballad", "sorrow", "grief", "pain", "hurt", "broken", "empty",
            "lost", "goodbye", "melancholy", "depress",
            "грусть", "слёзы", "печаль", "дождь", "один", "ночь", "прощай"
        )
        if (melancholicKeywords.any { combined.contains(it) }) {
            energy -= 0.45f
            valence -= 0.50f
        }

        // 5. Low Energy & Warm / Chill / Jazz / Ambient
        val chillKeywords = listOf(
            "chill", "relax", "jazz", "lofi", "lo-fi", "ambient", "lounge", "calm",
            "breeze", "peace", "sleep", "smooth", "coffee", "cozy", "reggae", "chillout",
            "джаз", "чилл", "спокойствие", "уют", "релакс"
        )
        if (chillKeywords.any { combined.contains(it) }) {
            energy -= 0.50f
            valence += 0.35f
        }

        // 6. Subtle deterministic dispersion based on song ID and title hash (only +/- 0.05f)
        val hash = (song.id.hashCode() xor song.title.hashCode())
        val hashValenceOffset = ((hash % 100).toFloat() / 2000f) // -0.05f .. +0.05f
        val hashEnergyOffset = (((hash / 100) % 100).toFloat() / 2000f)

        valence = (valence + hashValenceOffset).coerceIn(-1.0f, 1.0f)
        energy = (energy + hashEnergyOffset).coerceIn(-1.0f, 1.0f)

        // Safety guarantee: Never classify into REBEL_AGGRESSION unless explicitly matched
        if (!matchedAggressive && valence < 0f && energy >= 0f) {
            // Shift towards Warmth/Chill or Euphoria
            valence = 0.15f
        }

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
