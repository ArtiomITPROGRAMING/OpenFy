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

package com.example.openfy.core.audio.service

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Controller for Apple Music Sing-style real-time Karaoke vocal isolation & suppression.
 */
object VocalRemoverController {
    private val _isKaraokeEnabled = MutableStateFlow(false)
    val isKaraokeEnabled = _isKaraokeEnabled.asStateFlow()

    private val _vocalLevel = MutableStateFlow(0.05f) // 0.0f = pure instrumental, 1.0f = full original vocals
    val vocalLevel = _vocalLevel.asStateFlow()

    fun setKaraokeEnabled(enabled: Boolean) {
        _isKaraokeEnabled.value = enabled
    }

    fun setVocalLevel(level: Float) {
        _vocalLevel.value = level.coerceIn(0f, 1f)
    }

    fun toggleKaraoke() {
        _isKaraokeEnabled.value = !_isKaraokeEnabled.value
    }
}

/**
 * Real-time hardware-grade DSP Vocal Remover AudioProcessor for ExoPlayer.
 *
 * Uses Center-Channel Mid/Side cancellation with a 1-pole bass preservation filter
 * (keeps the kick drum & bassline punchy below 180Hz while eliminating lead vocals).
 */
@OptIn(UnstableApi::class)
class VocalRemoverAudioProcessor : BaseAudioProcessor() {

    private var lowMid: Float = 0f

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT || inputAudioFormat.channelCount != 2) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val outputBuffer = replaceOutputBuffer(remaining)

        val isKaraoke = VocalRemoverController.isKaraokeEnabled.value
        val vocalLevel = VocalRemoverController.vocalLevel.value

        if (!isKaraoke || vocalLevel >= 0.99f) {
            // Passthrough without processing
            outputBuffer.put(inputBuffer)
            outputBuffer.flip()
            return
        }

        // Process 16-bit PCM stereo sample pairs
        inputBuffer.order(ByteOrder.LITTLE_ENDIAN)
        outputBuffer.order(ByteOrder.LITTLE_ENDIAN)

        val alpha = 0.025f // ~180 Hz cutoff for 44.1/48kHz to preserve kick & bassline

        while (inputBuffer.remaining() >= 4) {
            val sL = inputBuffer.short.toFloat()
            val sR = inputBuffer.short.toFloat()

            val mid = (sL + sR) * 0.5f
            val side = (sL - sR) * 0.5f

            // Low-pass filter to retain bass & kick drum centered in the mix
            lowMid += alpha * (mid - lowMid)

            val vocalMid = mid - lowMid
            val processedMid = lowMid + vocalMid * vocalLevel

            val outL = (side + processedMid).coerceIn(-32768f, 32767f).toInt().toShort()
            val outR = (-side + processedMid).coerceIn(-32768f, 32767f).toInt().toShort()

            outputBuffer.putShort(outL)
            outputBuffer.putShort(outR)
        }

        outputBuffer.flip()
    }

    override fun onReset() {
        lowMid = 0f
    }
}
