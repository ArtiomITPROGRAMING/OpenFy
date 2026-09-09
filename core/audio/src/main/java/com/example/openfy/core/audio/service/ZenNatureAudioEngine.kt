package com.example.openfy.core.audio.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

enum class NatureSoundType(val displayName: String, val description: String) {
    NIGHT_RAIN("Ночной тёплый дождь", "Успокаивающий шум дождя с каплями для глубокого сна"),
    OCEAN_WAVES("Океанский прибой", "Ритмичные морские волны с плавной динамикой"),
    FOREST_WIND("Шелест леса и ветер", "Мягкие порывы ночного ветра и природный шелест"),
    PINK_NOISE("Глубокий розовый шум", "Акустический 1/f спектр для дельта-фазы сна")
}

/**
 * 100% Offline Procedural Sound Synthesizer for sleep & relaxation.
 * Generates natural acoustic soundscapes mathematically in real time without audio files or internet.
 */
class ZenNatureAudioEngine {

    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(4096)

    private var currentType: NatureSoundType = NatureSoundType.NIGHT_RAIN
    private var targetVolume: Float = 0.35f
    private var currentVolume: Float = 0.0f
    private var isPlaying: Boolean = false

    fun startNatureSound(type: NatureSoundType, volume: Float = 0.35f) {
        stop()
        currentType = type
        targetVolume = volume.coerceIn(0.05f, 1.0f)
        currentVolume = 0.01f // Start soft and fade in
        isPlaying = true

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            synthJob = scope.launch {
                synthesizeAudioLoop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun synthesizeAudioLoop() {
        val buffer = ShortArray(bufferSize / 2)
        val random = Random(System.currentTimeMillis())

        // Pink noise generator state
        var b0 = 0.0
        var b1 = 0.0
        var b2 = 0.0
        var b3 = 0.0
        var b4 = 0.0
        var b5 = 0.0
        var b6 = 0.0

        var waveTime = 0.0

        while (isPlaying && synthJob?.isActive == true) {
            // Smooth fade-in
            if (currentVolume < targetVolume) {
                currentVolume = (currentVolume + 0.005f).coerceAtMost(targetVolume)
            }

            for (i in buffer.indices) {
                val white = random.nextDouble() * 2.0 - 1.0

                // 1/f Pink Noise Filter
                b0 = 0.99886 * b0 + white * 0.0555179
                b1 = 0.99332 * b1 + white * 0.0750759
                b2 = 0.96900 * b2 + white * 0.1538520
                b3 = 0.86650 * b3 + white * 0.3104856
                b4 = 0.55000 * b4 + white * 0.5329522
                b5 = -0.7616 * b5 - white * 0.0168980
                val pink = (b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362) * 0.11
                b6 = white * 0.115926

                var sample = 0.0

                when (currentType) {
                    NatureSoundType.NIGHT_RAIN -> {
                        // Rain: Filtered pink noise + occasional raindrop clicks
                        val rainNoise = pink * 0.75 + white * 0.15
                        val drop = if (random.nextDouble() < 0.0008) (random.nextDouble() * 0.6) else 0.0
                        sample = (rainNoise + drop) * currentVolume
                    }
                    NatureSoundType.OCEAN_WAVES -> {
                        // Ocean: Pink noise modulated by low frequency sinusoidal wave surge (0.12 Hz)
                        waveTime += 1.0 / sampleRate
                        val waveMod = (sin(waveTime * 2.0 * PI * 0.12) * 0.5 + 0.5) * 0.7 + 0.3
                        val surfMod = (sin(waveTime * 2.0 * PI * 0.06 + 1.0) * 0.5 + 0.5) * 0.3
                        sample = pink * (waveMod + surfMod) * currentVolume * 1.2
                    }
                    NatureSoundType.FOREST_WIND -> {
                        // Wind: Slow rolling envelope on soft pink noise with breeze gusts
                        waveTime += 1.0 / sampleRate
                        val gust = (sin(waveTime * 2.0 * PI * 0.08) * 0.4 + sin(waveTime * 2.0 * PI * 0.03) * 0.4 + 0.5).coerceIn(0.1, 1.0)
                        sample = pink * gust * currentVolume * 0.95
                    }
                    NatureSoundType.PINK_NOISE -> {
                        sample = pink * currentVolume * 1.1
                    }
                }

                // Clamp to 16-bit PCM
                val pcmValue = (sample.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                buffer[i] = pcmValue
            }

            audioTrack?.write(buffer, 0, buffer.size)
        }
    }

    fun stop() {
        isPlaying = false
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
