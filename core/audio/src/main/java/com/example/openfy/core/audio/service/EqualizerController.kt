package com.example.openfy.core.audio.service

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    private var currentAudioSessionId: Int = 0

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

    init {
        _isEnabled.value = prefs.getBoolean(KEY_EQ_ENABLED, true)
        _virtualizerStrength.value = prefs.getInt(KEY_VIRTUALIZER_STRENGTH, 0)
        _bassBoostStrength.value = prefs.getInt(KEY_BASS_BOOST_STRENGTH, 0)
        _reverbPreset.value = prefs.getInt(KEY_REVERB_PRESET, PresetReverb.PRESET_NONE.toInt()).toShort()
        loadPresetsAndBands()
        restoreSavedBandLevels()
    }

    fun bindAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        if (currentAudioSessionId == audioSessionId && equalizer != null) return

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

        try {
            equalizer = try {
                Equalizer(1000, audioSessionId)
            } catch (e: Exception) {
                Equalizer(0, audioSessionId)
            }.apply {
                enabled = _isEnabled.value
            }

            virtualizer = try {
                Virtualizer(1000, audioSessionId).apply {
                    if (strengthSupported) {
                        setStrength(_virtualizerStrength.value.toShort())
                    }
                    enabled = _isEnabled.value
                }
            } catch (e: Exception) {
                try {
                    Virtualizer(0, audioSessionId).apply {
                        if (strengthSupported) {
                            setStrength(_virtualizerStrength.value.toShort())
                        }
                        enabled = _isEnabled.value
                    }
                } catch (e2: Exception) {
                    null
                }
            }

            bassBoost = try {
                BassBoost(1000, audioSessionId).apply {
                    if (strengthSupported) {
                        setStrength(_bassBoostStrength.value.toShort())
                    }
                    enabled = _isEnabled.value
                }
            } catch (e: Exception) {
                try {
                    BassBoost(0, audioSessionId).apply {
                        if (strengthSupported) {
                            setStrength(_bassBoostStrength.value.toShort())
                        }
                        enabled = _isEnabled.value
                    }
                } catch (e2: Exception) {
                    null
                }
            }

            presetReverb = try {
                PresetReverb(0, audioSessionId).apply {
                    preset = _reverbPreset.value
                    enabled = _isEnabled.value
                }
            } catch (e: Exception) {
                null
            }

            loadPresetsAndBands()
            restoreSavedBandLevels()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadPresetsAndBands() {
        val eq = equalizer
        if (eq == null) {
            // Default 5-band fallback for preview if audio session not yet started
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
            if (_presets.value.isEmpty()) {
                _presets.value = listOf("Flat", "Bass Boost", "Vocal Crystal", "Rock", "Electronic", "Acoustic", "Custom")
            }
            return
        }

        val numBands = eq.numberOfBands
        val minLevel = eq.bandLevelRange[0]
        val maxLevel = eq.bandLevelRange[1]

        val bandList = mutableListOf<BandInfo>()
        for (i in 0 until numBands) {
            val bandIdx = i.toShort()
            val centerFreq = eq.getCenterFreq(bandIdx) / 1000
            val level = eq.getBandLevel(bandIdx)
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

        // Presets
        val presetNames = mutableListOf<String>()
        presetNames.add("Flat")
        for (p in 0 until eq.numberOfPresets) {
            val pName = eq.getPresetName(p.toShort())
            if (!presetNames.contains(pName)) {
                presetNames.add(pName)
            }
        }
        val customPresets = listOf("Club Bass", "Vocal Crystal", "Electro Cyber", "Acoustic Warmth")
        customPresets.forEach {
            if (!presetNames.contains(it)) presetNames.add(it)
        }
        presetNames.add("Custom")
        _presets.value = presetNames
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

    fun setBandLevel(bandIndex: Short, levelMb: Short) {
        try {
            equalizer?.setBandLevel(bandIndex, levelMb)
            _bands.value = _bands.value.map {
                if (it.index == bandIndex) it.copy(currentLevelMb = levelMb) else it
            }
            _currentPreset.value = "Custom"

            val map = _bands.value.associate { it.index to it.currentLevelMb }
            prefs.edit().putString(KEY_BAND_LEVELS, gson.toJson(map)).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetToFlat() {
        _bands.value.forEach { band ->
            setBandLevel(band.index, 0)
        }
        _currentPreset.value = "Flat"
    }

    fun usePreset(presetName: String) {
        _currentPreset.value = presetName

        if (presetName.equals("Flat", ignoreCase = true)) {
            resetToFlat()
            return
        }

        // Custom built-in tuned presets
        when (presetName) {
            "Club Bass" -> {
                applyCustomGainMap(listOf(500, 350, 0, 100, 200))
                setBassBoostStrength(650)
                return
            }
            "Vocal Crystal" -> {
                applyCustomGainMap(listOf(-200, 100, 450, 350, 200))
                setVirtualizerStrength(300)
                return
            }
            "Electro Cyber" -> {
                applyCustomGainMap(listOf(450, 200, -100, 300, 500))
                setVirtualizerStrength(500)
                return
            }
            "Acoustic Warmth" -> {
                applyCustomGainMap(listOf(200, 300, 200, 100, -100))
                return
            }
        }

        val eq = equalizer ?: return
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
    }

    private fun applyCustomGainMap(gainsMb: List<Int>) {
        val current = _bands.value
        for (i in current.indices) {
            val gain = if (i < gainsMb.size) gainsMb[i].toShort() else (0).toShort()
            val clamped = gain.coerceIn(current[i].minLevelMb, current[i].maxLevelMb)
            setBandLevel(current[i].index, clamped)
        }
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        prefs.edit().putBoolean(KEY_EQ_ENABLED, enabled).apply()
        equalizer?.enabled = enabled
        virtualizer?.enabled = enabled
        bassBoost?.enabled = enabled
        presetReverb?.enabled = enabled
    }

    fun setVirtualizerStrength(strength: Int) {
        _virtualizerStrength.value = strength
        prefs.edit().putInt(KEY_VIRTUALIZER_STRENGTH, strength).apply()
        virtualizer?.let {
            if (it.strengthSupported) {
                try {
                    it.setStrength(strength.toShort())
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun setBassBoostStrength(strength: Int) {
        _bassBoostStrength.value = strength
        prefs.edit().putInt(KEY_BASS_BOOST_STRENGTH, strength).apply()
        bassBoost?.let {
            if (it.strengthSupported) {
                try {
                    it.setStrength(strength.toShort())
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun setReverbPreset(preset: Short) {
        _reverbPreset.value = preset
        prefs.edit().putInt(KEY_REVERB_PRESET, preset.toInt()).apply()
        presetReverb?.let {
            try {
                it.preset = preset
            } catch (e: Exception) {
                // Ignore
            }
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val KEY_EQ_ENABLED = "eq_enabled"
        private const val KEY_BAND_LEVELS = "eq_band_levels"
        private const val KEY_VIRTUALIZER_STRENGTH = "eq_virtualizer"
        private const val KEY_BASS_BOOST_STRENGTH = "eq_bass_boost"
        private const val KEY_REVERB_PRESET = "eq_reverb"
    }
}
