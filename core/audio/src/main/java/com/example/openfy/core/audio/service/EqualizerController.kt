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

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import androidx.media3.common.AuxEffectInfo
import androidx.media3.exoplayer.ExoPlayer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

data class BandInfo(
    val index: Short,
    val centerFreqHz: Int,
    val minLevelMb: Short,
    val maxLevelMb: Short,
    val currentLevelMb: Short
)

class EqualizerController(context: Context) {

    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences("openfy_equalizer", Context.MODE_PRIVATE)
    private val gson = Gson()

    private var equalizer: Equalizer? = null
    private var virtualizer: Virtualizer? = null
    private var bassBoost: BassBoost? = null
    private var presetReverb: PresetReverb? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var currentAudioSessionId: Int = 0
    private var attachedPlayer: ExoPlayer? = null

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _bands = MutableStateFlow<List<BandInfo>>(emptyList())
    val bands: StateFlow<List<BandInfo>> = _bands.asStateFlow()

    private val _presets = MutableStateFlow<List<String>>(emptyList())
    val presets: StateFlow<List<String>> = _presets.asStateFlow()

    private val _currentPreset = MutableStateFlow<String>("Flat")
    val currentPreset: StateFlow<String> = _currentPreset.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(0)
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()

    private val _bassBoostStrength = MutableStateFlow(0)
    val bassBoostStrength: StateFlow<Int> = _bassBoostStrength.asStateFlow()

    private val _reverbPreset = MutableStateFlow<Short>(PresetReverb.PRESET_NONE)
    val reverbPreset: StateFlow<Short> = _reverbPreset.asStateFlow()

    private val _loudnessGainMb = MutableStateFlow(0)
    val loudnessGainMb: StateFlow<Int> = _loudnessGainMb.asStateFlow()

    // Calibrated 5-band gain curves (in mB: 100 mB = 1 dB)
    private val tunedPresets = mapOf(
        "Flat" to listOf(0, 0, 0, 0, 0),
        "Бас" to listOf(600, 400, 100, 0, 0),
        "Клуб" to listOf(700, 500, 0, 200, 300),
        "Рок" to listOf(450, 200, -100, 250, 400),
        "Поп" to listOf(-150, 200, 400, 200, -100),
        "Электроника" to listOf(500, 350, 0, 300, 450),
        "Вокал" to listOf(-200, 100, 500, 400, 200),
        "Классика" to listOf(400, 250, -150, 250, 350),
        "Джаз" to listOf(350, 150, -100, 150, 300),
        "Акустика" to listOf(350, 250, 100, 250, 300),
        "Хип-хоп" to listOf(600, 350, 0, 150, 300),
        "Усиление ВЧ" to listOf(-200, 0, 150, 450, 700)
    )

    init {
        _isEnabled.value = prefs.getBoolean(KEY_EQ_ENABLED, true)
        _currentPreset.value = prefs.getString(KEY_CURRENT_PRESET, "Flat") ?: "Flat"
        _virtualizerStrength.value = prefs.getInt(KEY_VIRTUALIZER_STRENGTH, 0)
        _bassBoostStrength.value = prefs.getInt(KEY_BASS_BOOST_STRENGTH, 0)
        _reverbPreset.value = prefs.getInt(KEY_REVERB_PRESET, PresetReverb.PRESET_NONE.toInt()).toShort()
        _loudnessGainMb.value = prefs.getInt(KEY_LOUDNESS_GAIN, 0)

        // Initialize presets list
        val presetList = mutableListOf<String>()
        presetList.addAll(tunedPresets.keys)
        presetList.add("Пользовательский")
        _presets.value = presetList

        loadPresetsAndBands()
        restoreSavedBandLevels()
    }

    fun bindAudioSession(audioSessionId: Int, player: ExoPlayer? = null) {
        if (player != null) {
            attachedPlayer = player
        }

        if (audioSessionId <= 0) return
        if (currentAudioSessionId == audioSessionId && equalizer != null) {
            updatePlayerAuxEffect()
            return
        }

        release()
        currentAudioSessionId = audioSessionId

        try {
            val openIntent = Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, appContext.packageName)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            }
            appContext.sendBroadcast(openIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 1. Equalizer
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _isEnabled.value
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Virtualizer (Spatial Audio)
        try {
            virtualizer = Virtualizer(0, audioSessionId).apply {
                if (strengthSupported) {
                    setStrength(_virtualizerStrength.value.toShort())
                }
                enabled = _isEnabled.value && _virtualizerStrength.value > 0
            }
        } catch (e: Exception) {
            virtualizer = null
        }

        // 3. Bass Boost
        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                if (strengthSupported) {
                    setStrength(_bassBoostStrength.value.toShort())
                }
                enabled = _isEnabled.value && _bassBoostStrength.value > 0
            }
        } catch (e: Exception) {
            bassBoost = null
        }

        // 4. Loudness Enhancer (Pre-Amp Volume Booster)
        try {
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                setTargetGain(_loudnessGainMb.value)
                enabled = _isEnabled.value && _loudnessGainMb.value > 0
            }
        } catch (e: Exception) {
            loudnessEnhancer = null
        }

        // 5. Preset Reverb (Auxiliary Audio Effect)
        try {
            presetReverb = PresetReverb(0, 0).apply {
                preset = _reverbPreset.value
                enabled = _isEnabled.value && _reverbPreset.value != PresetReverb.PRESET_NONE
            }
        } catch (e: Exception) {
            try {
                presetReverb = PresetReverb(0, audioSessionId).apply {
                    preset = _reverbPreset.value
                    enabled = _isEnabled.value && _reverbPreset.value != PresetReverb.PRESET_NONE
                }
            } catch (e2: Exception) {
                presetReverb = null
            }
        }

        loadPresetsAndBands()
        restoreSavedBandLevels()
        updatePlayerAuxEffect()
    }

    private fun loadPresetsAndBands() {
        val eq = equalizer
        if (eq == null) {
            // 5-band default fallback for preview when audio session is not yet active
            if (_bands.value.isEmpty()) {
                val fallbackBands = listOf(
                    BandInfo(0, 60, -1500, 1500, 0),
                    BandInfo(1, 230, -1500, 1500, 0),
                    BandInfo(2, 910, -1500, 1500, 0),
                    BandInfo(3, 3600, -1500, 1500, 0),
                    BandInfo(4, 14000, -1500, 1500, 0)
                )
                _bands.value = fallbackBands
            }
            return
        }

        try {
            val numBands = eq.numberOfBands
            val minLevel = eq.bandLevelRange[0]
            val maxLevel = eq.bandLevelRange[1]

            val bandList = mutableListOf<BandInfo>()
            for (i in 0 until numBands) {
                val bandIdx = i.toShort()
                val centerFreq = eq.getCenterFreq(bandIdx) / 1000
                val level = try { eq.getBandLevel(bandIdx) } catch (_: Exception) { 0.toShort() }
                bandList.add(
                    BandInfo(
                        index = bandIdx,
                        centerFreqHz = centerFreq,
                        minLevelMb = minLevel,
                        maxLevelMb = maxLevel,
                        currentLevelMb = level
                    )
                )
            }
            _bands.value = bandList

            // Combine built-in tuned presets with any hardware device presets
            val presetNames = mutableListOf<String>()
            presetNames.addAll(tunedPresets.keys)
            for (p in 0 until eq.numberOfPresets) {
                val pName = try { eq.getPresetName(p.toShort()) } catch (_: Exception) { null }
                if (pName != null && !presetNames.contains(pName)) {
                    presetNames.add(pName)
                }
            }
            if (!presetNames.contains("Пользовательский")) {
                presetNames.add("Пользовательский")
            }
            _presets.value = presetNames
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun restoreSavedBandLevels() {
        val savedJson = prefs.getString(KEY_BAND_LEVELS, null)
        if (savedJson != null) {
            val type = object : TypeToken<Map<Short, Short>>() {}.type
            val savedMap: Map<Short, Short> = gson.fromJson(savedJson, type) ?: emptyMap()
            equalizer?.let { eq ->
                for ((band, level) in savedMap) {
                    try {
                        eq.setBandLevel(band, level)
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
            // Update bands flow
            val updated = _bands.value.map { band ->
                savedMap[band.index]?.let { band.copy(currentLevelMb = it) } ?: band
            }
            _bands.value = updated
        }
    }

    fun setBandLevel(bandIndex: Short, levelMb: Short, fromUserSlider: Boolean = true) {
        try {
            equalizer?.setBandLevel(bandIndex, levelMb)
            _bands.value = _bands.value.map {
                if (it.index == bandIndex) it.copy(currentLevelMb = levelMb) else it
            }

            if (fromUserSlider) {
                _currentPreset.value = "Пользовательский"
                prefs.edit().putString(KEY_CURRENT_PRESET, "Пользовательский").apply()
            }

            val map = _bands.value.associate { it.index to it.currentLevelMb }
            prefs.edit().putString(KEY_BAND_LEVELS, gson.toJson(map)).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetToFlat() {
        val current = _bands.value
        current.forEach { band ->
            setBandLevel(band.index, 0, fromUserSlider = false)
        }
        setBassBoostStrength(0)
        setVirtualizerStrength(0)
        _currentPreset.value = "Flat"
        prefs.edit().putString(KEY_CURRENT_PRESET, "Flat").apply()
    }

    fun usePreset(presetName: String) {
        _currentPreset.value = presetName
        prefs.edit().putString(KEY_CURRENT_PRESET, presetName).apply()

        if (presetName.equals("Flat", ignoreCase = true)) {
            resetToFlat()
            return
        }

        // 1. Check calibrated tuned presets
        val customCurve = tunedPresets[presetName]
        if (customCurve != null) {
            applyCustomGainMap(customCurve)
            if (presetName == "Бас") {
                setBassBoostStrength(600)
            } else if (presetName == "Клуб") {
                setBassBoostStrength(750)
            } else if (presetName == "Вокал") {
                setVirtualizerStrength(350)
            } else if (presetName == "Электроника") {
                setVirtualizerStrength(450)
            }
            return
        }

        // 2. Check hardware device presets
        val eq = equalizer ?: return
        try {
            for (p in 0 until eq.numberOfPresets) {
                val name = eq.getPresetName(p.toShort())
                if (name.equals(presetName, ignoreCase = true)) {
                    eq.usePreset(p.toShort())
                    loadPresetsAndBands()
                    val map = _bands.value.associate { it.index to it.currentLevelMb }
                    prefs.edit().putString(KEY_BAND_LEVELS, gson.toJson(map)).apply()
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyCustomGainMap(gainsMb: List<Int>) {
        val current = _bands.value
        for (i in current.indices) {
            val gain = if (i < gainsMb.size) gainsMb[i].toShort() else (0).toShort()
            val clamped = gain.coerceIn(current[i].minLevelMb, current[i].maxLevelMb)
            setBandLevel(current[i].index, clamped, fromUserSlider = false)
        }
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        prefs.edit().putBoolean(KEY_EQ_ENABLED, enabled).apply()
        equalizer?.enabled = enabled
        virtualizer?.enabled = enabled && _virtualizerStrength.value > 0
        bassBoost?.enabled = enabled && _bassBoostStrength.value > 0
        loudnessEnhancer?.enabled = enabled && _loudnessGainMb.value > 0
        presetReverb?.enabled = enabled && _reverbPreset.value != PresetReverb.PRESET_NONE
        updatePlayerAuxEffect()
    }

    fun setVirtualizerStrength(strength: Int) {
        _virtualizerStrength.value = strength
        prefs.edit().putInt(KEY_VIRTUALIZER_STRENGTH, strength).apply()
        virtualizer?.let {
            try {
                it.enabled = _isEnabled.value && strength > 0
                if (it.strengthSupported) {
                    it.setStrength(strength.toShort())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setBassBoostStrength(strength: Int) {
        _bassBoostStrength.value = strength
        prefs.edit().putInt(KEY_BASS_BOOST_STRENGTH, strength).apply()
        bassBoost?.let {
            try {
                it.enabled = _isEnabled.value && strength > 0
                if (it.strengthSupported) {
                    it.setStrength(strength.toShort())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setLoudnessGain(gainMb: Int) {
        _loudnessGainMb.value = gainMb
        prefs.edit().putInt(KEY_LOUDNESS_GAIN, gainMb).apply()
        loudnessEnhancer?.let {
            try {
                it.setTargetGain(gainMb)
                it.enabled = _isEnabled.value && gainMb > 0
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setReverbPreset(preset: Short) {
        _reverbPreset.value = preset
        prefs.edit().putInt(KEY_REVERB_PRESET, preset.toInt()).apply()
        presetReverb?.let {
            try {
                it.preset = preset
                it.enabled = _isEnabled.value && preset != PresetReverb.PRESET_NONE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        updatePlayerAuxEffect()
    }

    private fun updatePlayerAuxEffect() {
        val player = attachedPlayer ?: return
        val reverb = presetReverb
        try {
            if (_isEnabled.value && reverb != null && _reverbPreset.value != PresetReverb.PRESET_NONE) {
                player.setAuxEffectInfo(AuxEffectInfo(reverb.id, 1.0f))
            } else {
                player.setAuxEffectInfo(AuxEffectInfo(AuxEffectInfo.NO_AUX_EFFECT_ID, 0f))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        if (currentAudioSessionId > 0) {
            try {
                val closeIntent = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
                    putExtra(AudioEffect.EXTRA_AUDIO_SESSION, currentAudioSessionId)
                    putExtra(AudioEffect.EXTRA_PACKAGE_NAME, appContext.packageName)
                }
                appContext.sendBroadcast(closeIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        try {
            equalizer?.release()
            equalizer = null
            virtualizer?.release()
            virtualizer = null
            bassBoost?.release()
            bassBoost = null
            presetReverb?.release()
            presetReverb = null
            loudnessEnhancer?.release()
            loudnessEnhancer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val KEY_EQ_ENABLED = "eq_enabled"
        private const val KEY_CURRENT_PRESET = "eq_current_preset"
        private const val KEY_BAND_LEVELS = "eq_band_levels"
        private const val KEY_VIRTUALIZER_STRENGTH = "eq_virtualizer"
        private const val KEY_BASS_BOOST_STRENGTH = "eq_bass_boost"
        private const val KEY_REVERB_PRESET = "eq_reverb"
        private const val KEY_LOUDNESS_GAIN = "eq_loudness_gain"
    }
}
