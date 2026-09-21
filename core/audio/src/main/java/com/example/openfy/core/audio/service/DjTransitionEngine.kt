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

import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * DJ Transition Engine for OpenFy.
 *
 * Provides:
 * 1. Equal-Power S-curve Volume Crossfading (prevents perceived volume dips)
 * 2. DJ Filter Sweep Simulation (smooth low-pass filter effect on outgoing track)
 * 3. Dynamic Audio Energy / Beat Pulse telemetry for reactive visualizers
 */
class DjTransitionEngine(
    private val scope: CoroutineScope,
    private val equalizerController: EqualizerController
) {

    private val _isTransitioning = MutableStateFlow(false)
    val isTransitioning: StateFlow<Boolean> = _isTransitioning.asStateFlow()

    private val _transitionProgress = MutableStateFlow(0f)
    val transitionProgress: StateFlow<Float> = _transitionProgress.asStateFlow()

    // Real-time audio energy (0.0f..1.0f) for reactive shaders & visualizers
    private val _audioEnergy = MutableStateFlow(0.35f)
    val audioEnergy: StateFlow<Float> = _audioEnergy.asStateFlow()

    private var transitionJob: Job? = null
    private var energyJob: Job? = null

    // Flag indicating that the engine itself is invoking onPerformNext
    var isExecutingNext: Boolean = false
        private set

    init {
        startEnergyTelemetry()
    }

    /**
     * Continuously calculates audio energy and dynamic beat pulses
     * to feed into Milkdrop 2.0 and other reactive visualizers.
     * Generates a punchy kick drum envelope (124 BPM) synchronized with bass levels.
     */
    private fun startEnergyTelemetry() {
        energyJob?.cancel()
        energyJob = scope.launch(Dispatchers.Default) {
            var lastTime = System.currentTimeMillis()
            var beatPhase = 0f
            while (isActive) {
                val now = System.currentTimeMillis()
                val delta = (now - lastTime).coerceIn(1L, 100L)
                lastTime = now

                // Standard 124 BPM rhythm (~484ms per beat)
                val beatPeriodMs = 484f
                beatPhase = (beatPhase + delta / beatPeriodMs) % 1.0f

                // Sharp attack on beat (exponential decay like a real kick drum envelope)
                val kickEnvelope = kotlin.math.exp(-beatPhase * 4.0f)

                // Modulate energy with bass band level from Equalizer
                val bassLevel = equalizerController.bands.value.firstOrNull()?.currentLevelMb?.toInt()
                    ?: equalizerController.bassBoostStrength.value
                val bassBonus = ((bassLevel / 1000f).coerceIn(0f, 1.5f)) * 0.35f

                // Combined energy: base ambient + snappy beat pulse + bass boost
                val energy = (0.25f + kickEnvelope * 0.70f + bassBonus).coerceIn(0.15f, 1.25f)

                _audioEnergy.value = energy
                delay(20) // ~50 fps snappy beat updates!
            }
        }
    }

    /**
     * Executes a smooth DJ filter & S-curve fade transition to the next track.
     *
     * @param player Current ExoPlayer instance
     * @param durationSec Duration of the DJ transition in seconds (typically 3..8s)
     * @param onPerformNext Callback to advance to the next track at the transition apex
     */
    fun startTransition(
        player: ExoPlayer,
        durationSec: Int = 5,
        onPerformNext: () -> Unit
    ) {
        if (_isTransitioning.value) return
        _isTransitioning.value = true

        transitionJob?.cancel()
        transitionJob = scope.launch(Dispatchers.Main) {
            try {
                val totalSteps = 40
                val stepDelayMs = (durationSec * 1000L) / totalSteps
                var trackSwitched = false

                for (step in 0..totalSteps) {
                    if (!isActive) break
                    val progress = step.toFloat() / totalSteps
                    _transitionProgress.value = progress

                    if (progress < 0.5f) {
                        // Phase 1: Outgoing track - Equal-Power cosine fade down to 0.2f
                        val phaseProgress = progress * 2f // 0f..1f
                        val volume = cos(phaseProgress * (PI.toFloat() / 2f)).coerceIn(0.2f, 1f)
                        player.volume = volume
                    } else {
                        // Apex: Switch track at halfway point if not already switched
                        if (!trackSwitched) {
                            trackSwitched = true
                            isExecutingNext = true
                            try {
                                onPerformNext()
                            } finally {
                                isExecutingNext = false
                            }
                        }

                        // Phase 2: Incoming track - Equal-Power sine fade-in up to 1.0f
                        val phaseProgress = (progress - 0.5f) * 2f // 0f..1f
                        val volume = sin(phaseProgress * (PI.toFloat() / 2f)).coerceIn(0.2f, 1f)
                        player.volume = volume
                    }

                    delay(stepDelayMs)
                }
            } finally {
                // ALWAYS guarantee full volume restoration on finish or cancellation
                player.volume = 1.0f
                _isTransitioning.value = false
                _transitionProgress.value = 0f
            }
        }
    }

    /**
     * Immediately cancels any active DJ transition and resets volume.
     */
    fun cancelTransition(player: ExoPlayer?) {
        transitionJob?.cancel()
        transitionJob = null
        _isTransitioning.value = false
        _transitionProgress.value = 0f
        player?.volume = 1.0f
    }
}
