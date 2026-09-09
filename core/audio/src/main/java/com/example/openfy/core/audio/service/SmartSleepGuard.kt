package com.example.openfy.core.audio.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.sqrt

/**
 * Smart Sleep & Ear Protection Algorithm (SomnoGuard).
 */
class SmartSleepGuard(
    private val context: Context,
    private val playbackManager: PlaybackManager,
    private val natureAudioEngine: ZenNatureAudioEngine
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val scope = CoroutineScope(Dispatchers.Main)
    private var guardJob: Job? = null

    private var lastMotionTimestamp = System.currentTimeMillis()
    private var lastAccelMagnitude = 9.8f
    private val motionThreshold = 0.35f

    private val _isSleepDetected = MutableStateFlow(false)
    val isSleepDetected: StateFlow<Boolean> = _isSleepDetected.asStateFlow()

    private val _isNatureSoundPlaying = MutableStateFlow(false)
    val isNatureSoundPlaying: StateFlow<Boolean> = _isNatureSoundPlaying.asStateFlow()

    private var checkIntervalMs = 30_000L

    fun start() {
        stop()
        lastMotionTimestamp = System.currentTimeMillis()
        _isSleepDetected.value = false

        try {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        guardJob = scope.launch {
            while (true) {
                delay(checkIntervalMs)
                evaluateSleepState()
            }
        }
    }

    private fun evaluateSleepState() {
        val settings = playbackManager.settingsRepository
        if (!settings.smartSleepGuardEnabled.value) return
        if (!playbackManager.isPlaying.value) return

        val inactivityLimitMs = settings.smartSleepInactivityMinutes.value * 60 * 1000L
        val currentStillnessMs = System.currentTimeMillis() - lastMotionTimestamp

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNightWindow = hour >= 22 || hour < 8

        if (currentStillnessMs >= inactivityLimitMs) {
            triggerSleepTransition()
        }
    }

    private fun triggerSleepTransition() {
        if (_isSleepDetected.value) return
        _isSleepDetected.value = true

        scope.launch {
            val steps = 30
            for (step in steps downTo 0) {
                val factor = (step.toFloat() / steps.toFloat()) * (step.toFloat() / steps.toFloat())
                playbackManager.player?.volume = factor
                delay(1000L)
            }

            playbackManager.player?.pause()
            playbackManager.player?.volume = 1.0f

            val natureType = playbackManager.settingsRepository.smartSleepNatureSound.value
            natureAudioEngine.startNatureSound(natureType, volume = 0.28f)
            _isNatureSoundPlaying.value = true

            delay(25 * 60 * 1000L)
            natureAudioEngine.stop()
            _isNatureSoundPlaying.value = false
        }
    }

    fun stop() {
        guardJob?.cancel()
        guardJob = null
        try {
            sensorManager?.unregisterListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        natureAudioEngine.stop()
        _isNatureSoundPlaying.value = false
        _isSleepDetected.value = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val currentMagnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = kotlin.math.abs(currentMagnitude - lastAccelMagnitude)

            if (delta > motionThreshold) {
                lastMotionTimestamp = System.currentTimeMillis()
                if (_isSleepDetected.value) {
                    natureAudioEngine.stop()
                    _isNatureSoundPlaying.value = false
                    _isSleepDetected.value = false
                }
            }
            lastAccelMagnitude = currentMagnitude
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
