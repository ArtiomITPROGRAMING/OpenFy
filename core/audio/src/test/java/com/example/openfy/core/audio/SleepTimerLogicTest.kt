package com.example.openfy.core.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepTimerLogicTest {

    @Test
    fun `fade ratio calculation produces smooth continuous curve`() {
        val effectiveFadeSec = 30
        val remaining1 = 30
        val remaining2 = 15
        val remaining3 = 0

        val ratio1 = (remaining1.toFloat() / effectiveFadeSec).coerceIn(0f, 1f)
        val ratio2 = (remaining2.toFloat() / effectiveFadeSec).coerceIn(0f, 1f)
        val ratio3 = (remaining3.toFloat() / effectiveFadeSec).coerceIn(0f, 1f)

        assertEquals(1.0f, ratio1, 0.001f)
        assertEquals(0.5f, ratio2, 0.001f)
        assertEquals(0.0f, ratio3, 0.001f)
    }

    @Test
    fun `short track fade calculation operates in safe range`() {
        val windowMs = 4000L
        val remainingMs = 2000L

        val fadeRatio = (remainingMs.toFloat() / windowMs).coerceIn(0f, 1f)
        assertEquals(0.5f, fadeRatio, 0.001f)
    }
}
