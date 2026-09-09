package com.example.openfy.features.themes

import com.example.openfy.features.themes.engine.AppIconManager
import com.example.openfy.features.themes.engine.AppLauncherIcon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppIconManagerTest {

    @Test
    fun `AppLauncherIcon entries have valid alias suffixes and keys`() {
        assertEquals(4, AppLauncherIcon.entries.size)

        val neon = AppLauncherIcon.NEON_CORE
        assertEquals("MainActivityNeon", neon.aliasSuffix)
        assertEquals("NEON_CORE", neon.key)

        val titanium = AppLauncherIcon.TITANIUM_STUDIO
        assertEquals("MainActivityTitanium", titanium.aliasSuffix)
        assertEquals("TITANIUM_STUDIO", titanium.key)

        val spectrum = AppLauncherIcon.SPECTRUM_WAVE
        assertEquals("MainActivitySpectrum", spectrum.aliasSuffix)
        assertEquals("SPECTRUM_WAVE", spectrum.key)

        val midnight = AppLauncherIcon.MIDNIGHT_PLAY
        assertEquals("MainActivityMidnight", midnight.aliasSuffix)
        assertEquals("MIDNIGHT_PLAY", midnight.key)
    }

    @Test
    fun `fromKey parses valid keys and falls back gracefully to default MIDNIGHT_PLAY`() {
        assertEquals(AppLauncherIcon.TITANIUM_STUDIO, AppLauncherIcon.fromKey("TITANIUM_STUDIO"))
        assertEquals(AppLauncherIcon.SPECTRUM_WAVE, AppLauncherIcon.fromKey("spectrum_wave"))
        assertEquals(AppLauncherIcon.MIDNIGHT_PLAY, AppLauncherIcon.fromKey("MIDNIGHT_PLAY"))
        assertEquals(AppLauncherIcon.MIDNIGHT_PLAY, AppLauncherIcon.fromKey("unknown_invalid_key"))
        assertEquals(AppLauncherIcon.MIDNIGHT_PLAY, AppLauncherIcon.fromKey(null))
        assertEquals(AppLauncherIcon.DEFAULT, AppLauncherIcon.MIDNIGHT_PLAY)
    }
}
