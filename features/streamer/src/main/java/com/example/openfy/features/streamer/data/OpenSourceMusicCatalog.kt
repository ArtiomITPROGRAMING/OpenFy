package com.example.openfy.features.streamer.data

import com.example.openfy.core.audio.model.Song

data class OpenSourceTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val streamUrl: String,
    val coverUrl: String,
    val durationMs: Long,
    val license: String = "Creative Commons",
    val description: String = ""
) {
    fun toSong(): Song = Song.createStreamTrack(
        url = streamUrl,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        coverUrl = coverUrl
    )
}

object OpenSourceMusicCatalog {

    val GENRES = listOf(
        "Все",
        "Lo-Fi",
        "Synthwave",
        "Chillout",
        "Ambient",
        "Electronic",
        "Classical",
        "Jazz",
        "Rock"
    )

    val TRACKS: List<OpenSourceTrack> = listOf(
        // === Lo-Fi & Chill Beats ===
        OpenSourceTrack(
            id = "lofi_01",
            title = "Midnight Coffee",
            artist = "Lofi Sleep Chill",
            album = "Lo-Fi Study Beats (CC)",
            genre = "Lo-Fi",
            streamUrl = "https://cdn.pixabay.com/download/audio/2022/05/27/audio_1808fbf07a.mp3",
            coverUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=500&auto=format&fit=crop&q=80",
            durationMs = 142000L,
            license = "CC0 / Pixabay Audio",
            description = "Уютный мягкий Lo-Fi бит для концентрации и отдыха"
        ),
        OpenSourceTrack(
            id = "lofi_02",
            title = "Rainy Street Memories",
            artist = "Aesthetic Beats",
            album = "Midnight Tokyo Lo-Fi",
            genre = "Lo-Fi",
            streamUrl = "https://cdn.pixabay.com/download/audio/2022/01/18/audio_d0a13f69d2.mp3",
            coverUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?w=500&auto=format&fit=crop&q=80",
            durationMs = 158000L,
            license = "CC0 / Pixabay Audio",
            description = "Атмосферный ночной Lo-Fi с легким шумом дождя"
        ),
        OpenSourceTrack(
            id = "lofi_03",
            title = "Tokyo Sunset Cafe",
            artist = "Chilled Vibes",
            album = "Lo-Fi Sessions",
            genre = "Lo-Fi",
            streamUrl = "https://cdn.pixabay.com/download/audio/2022/10/14/audio_9939f792cb.mp3",
            coverUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=500&auto=format&fit=crop&q=80",
            durationMs = 135000L,
            license = "CC0 / Pixabay Audio",
            description = "Спокойный теплый Lo-Fi с винтажным пианино"
        ),

        // === Synthwave & Cyberpunk ===
        OpenSourceTrack(
            id = "synth_01",
            title = "Neon Drive 1984",
            artist = "Cyber Pulse",
            album = "Outrun Horizons (Open Audio)",
            genre = "Synthwave",
            streamUrl = "https://cdn.pixabay.com/download/audio/2022/03/15/audio_c8c8a73467.mp3",
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&auto=format&fit=crop&q=80",
            durationMs = 210000L,
            license = "CC0 / Pixabay Audio",
            description = "Энергичный 80s Synthwave с аналоговыми синтами"
        ),
        OpenSourceTrack(
            id = "synth_02",
            title = "Night City Highway",
            artist = "RetroWave Project",
            album = "Cyberpunk Dreams",
            genre = "Synthwave",
            streamUrl = "https://cdn.pixabay.com/download/audio/2021/09/06/audio_34b3e8e169.mp3",
            coverUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop&q=80",
            durationMs = 185000L,
            license = "CC0 / Pixabay Audio",
            description = "Динамичный футуристичный ретровейв"
        ),
        OpenSourceTrack(
            id = "synth_03",
            title = "Laser Grid",
            artist = "Vektor Neon",
            album = "Synth Dimension",
            genre = "Synthwave",
            streamUrl = "https://cdn.pixabay.com/download/audio/2023/02/28/audio_5514f77c38.mp3",
            coverUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=500&auto=format&fit=crop&q=80",
            durationMs = 196000L,
            license = "CC0 / Pixabay Audio",
            description = "Космический синтвейв для драйва"
        ),

        // === Chillout & Downtempo ===
        OpenSourceTrack(
            id = "chill_01",
            title = "Floating in Clouds",
            artist = "Horizon Glow",
            album = "Chill Sanctuary",
            genre = "Chillout",
            streamUrl = "https://cdn.pixabay.com/download/audio/2022/11/06/audio_c976939529.mp3",
            coverUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500&auto=format&fit=crop&q=80",
            durationMs = 175000L,
            license = "CC0 / Pixabay Audio",
            description = "Мягкий чиллаут с атмосферными пэдами"
        ),
        OpenSourceTrack(
            id = "chill_02",
            title = "Summer Breeze Lounge",
            artist = "Chillout Collective",
            album = "Sunset Island",
            genre = "Chillout",
            streamUrl = "https://cdn.pixabay.com/download/audio/2022/01/26/audio_d0c6ff1101.mp3",
            coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&auto=format&fit=crop&q=80",
            durationMs = 205000L,
            license = "CC0 / Pixabay Audio",
            description = "Расслабляющая легкая лаунж-мелодия"
        ),

        // === Ambient & Deep Focus ===
        OpenSourceTrack(
            id = "amb_01",
            title = "Cosmic Aurora",
            artist = "Astral Drift",
            album = "Deep Space Meditations",
            genre = "Ambient",
            streamUrl = "https://cdn.pixabay.com/download/audio/2021/11/25/audio_942666ffd5.mp3",
            coverUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=500&auto=format&fit=crop&q=80",
            durationMs = 240000L,
            license = "CC0 / Pixabay Audio",
            description = "Глубокий эмбиент для медитации и глубокой работы"
        ),
        OpenSourceTrack(
            id = "amb_02",
            title = "Zen Garden Serenity",
            artist = "Mind Calm",
            album = "Peaceful Mind",
            genre = "Ambient",
            streamUrl = "https://cdn.pixabay.com/download/audio/2022/02/07/audio_d0c6ff1101.mp3",
            coverUrl = "https://images.unsplash.com/photo-1518241353330-0f7941c2d9b5?w=500&auto=format&fit=crop&q=80",
            durationMs = 220000L,
            license = "CC0 / Pixabay Audio",
            description = "Медитативные звуки гармонии и природы"
        ),

        // === Electronic & Future Bass ===
        OpenSourceTrack(
            id = "elec_01",
            title = "Future Horizons",
            artist = "Glitch Dream",
            album = "Electronic Universe",
            genre = "Electronic",
            streamUrl = "https://cdn.pixabay.com/download/audio/2022/03/10/audio_c3527e30c2.mp3",
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80",
            durationMs = 182000L,
            license = "CC0 / Pixabay Audio",
            description = "Современный электронный трек с сочным басом"
        ),
        OpenSourceTrack(
            id = "elec_02",
            title = "Starlight Beats",
            artist = "Electro Nova",
            album = "Cyber Pulse 2026",
            genre = "Electronic",
            streamUrl = "https://cdn.pixabay.com/download/audio/2022/08/02/audio_884fe92c21.mp3",
            coverUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=500&auto=format&fit=crop&q=80",
            durationMs = 164000L,
            license = "CC0 / Pixabay Audio",
            description = "Динамичный электронный саундтрек"
        ),

        // === Classical Masterpieces (Open Audio / Public Domain) ===
        OpenSourceTrack(
            id = "class_01",
            title = "Clair de Lune (Debussy)",
            artist = "Claude Debussy (Perf. Markus Staab)",
            album = "Classical Open Collection",
            genre = "Classical",
            streamUrl = "https://upload.wikimedia.org/wikipedia/commons/2/29/Clair_de_lune_%28Debussy%29.ogg",
            coverUrl = "https://images.unsplash.com/photo-1520523839898-507128fc536a?w=500&auto=format&fit=crop&q=80",
            durationMs = 304000L,
            license = "Public Domain (CC-PD)",
            description = "Бессмертный шедевр импрессионизма для фортепиано"
        ),
        OpenSourceTrack(
            id = "class_02",
            title = "Moonlight Sonata (Beethoven)",
            artist = "Ludwig van Beethoven (Perf. Paul Pitman)",
            album = "Piano Sonatas",
            genre = "Classical",
            streamUrl = "https://upload.wikimedia.org/wikipedia/commons/e/eb/Beethoven_Moonlight_1st_movement.ogg",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=80",
            durationMs = 315000L,
            license = "Public Domain (CC-PD)",
            description = "Лунная соната (1-я часть, Adagio sostenuto)"
        ),
        OpenSourceTrack(
            id = "class_03",
            title = "Nocturne Op. 9 No. 2 (Chopin)",
            artist = "Frédéric Chopin (Open Piano)",
            album = "Romantic Piano Works",
            genre = "Classical",
            streamUrl = "https://upload.wikimedia.org/wikipedia/commons/3/30/Chopin_Nocturne_Op_9_No_2.ogg",
            coverUrl = "https://images.unsplash.com/photo-1513883049090-d0b7439799bf?w=500&auto=format&fit=crop&q=80",
            durationMs = 270000L,
            license = "Public Domain (CC-PD)",
            description = "Знаменитый ноктюрн ми-бемоль мажор Шопена"
        ),

        // === Jazz & Blues ===
        OpenSourceTrack(
            id = "jazz_01",
            title = "Autumn in Manhattan",
            artist = "Blue Note Quartet",
            album = "Jazz Evening",
            genre = "Jazz",
            streamUrl = "https://cdn.pixabay.com/download/audio/2022/05/16/audio_c89b25208f.mp3",
            coverUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=500&auto=format&fit=crop&q=80",
            durationMs = 195000L,
            license = "CC0 / Pixabay Audio",
            description = "Теплый вечерний джаз с саксофоном и контрабасом"
        ),
        OpenSourceTrack(
            id = "jazz_02",
            title = "Smooth Velvet Trio",
            artist = "Downtown Trio",
            album = "Jazz Bar Sessions",
            genre = "Jazz",
            streamUrl = "https://cdn.pixabay.com/download/audio/2022/03/24/audio_03d9876fa5.mp3",
            coverUrl = "https://images.unsplash.com/photo-1415201364774-f6f0bb35f28f?w=500&auto=format&fit=crop&q=80",
            durationMs = 180000L,
            license = "CC0 / Pixabay Audio",
            description = "Уютный лаунж-джаз для релаксации"
        ),

        // === Rock & Indie ===
        OpenSourceTrack(
            id = "rock_01",
            title = "Desert Highway Drive",
            artist = "Garage Band Project",
            album = "Indie Rock Wave",
            genre = "Rock",
            streamUrl = "https://cdn.pixabay.com/download/audio/2022/04/27/audio_65b3820295.mp3",
            coverUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500&auto=format&fit=crop&q=80",
            durationMs = 172000L,
            license = "CC0 / Pixabay Audio",
            description = "Драйвовый инди-рок с перегруженными гитарами"
        ),
        OpenSourceTrack(
            id = "rock_02",
            title = "Electric Sunrise",
            artist = "Rebel Sound",
            album = "High Voltage",
            genre = "Rock",
            streamUrl = "https://cdn.pixabay.com/download/audio/2022/10/25/audio_e6e5d0dbcf.mp3",
            coverUrl = "https://images.unsplash.com/photo-1464375117522-1311d6a5b81f?w=500&auto=format&fit=crop&q=80",
            durationMs = 188000L,
            license = "CC0 / Pixabay Audio",
            description = "Энергичный современный рок-ритм"
        )
    )

    fun getTracksByGenre(genre: String?): List<Song> {
        val tracks = if (genre.isNullOrBlank() || genre.equals("Все", ignoreCase = true)) {
            TRACKS
        } else {
            TRACKS.filter { it.genre.equals(genre, ignoreCase = true) }
        }
        return tracks.map { it.toSong() }
    }

    fun searchTracks(query: String): List<Song> {
        val clean = query.trim().lowercase()
        if (clean.isBlank()) return TRACKS.map { it.toSong() }

        return TRACKS.filter {
            it.title.lowercase().contains(clean) ||
            it.artist.lowercase().contains(clean) ||
            it.album.lowercase().contains(clean) ||
            it.genre.lowercase().contains(clean) ||
            it.description.lowercase().contains(clean)
        }.map { it.toSong() }
    }
}
