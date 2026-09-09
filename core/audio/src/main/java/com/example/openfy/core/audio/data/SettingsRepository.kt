package com.example.openfy.core.audio.data

import android.content.Context
import android.content.SharedPreferences
import com.example.openfy.core.audio.model.RepeatMode
import com.example.openfy.core.audio.service.NatureSoundType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeStyle(
    val displayName: String,
    val description: String,
    val hasBlobs: Boolean,
    val hasNeonGlow: Boolean
) {
    SERIOUS_DARK(
        "Мрачный Монолит (AMOLED)",
        "Истинный глубокий чёрный фон, без неона и блобов, строгие титаново-стальные карточки",
        hasBlobs = false,
        hasNeonGlow = false
    ),
    MIDNIGHT_NEON(
        "Midnight Neon Glass",
        "Стекломорфизм, анимированные плавающие неоновые блобы (Cyan/Purple/Pink) и светящиеся контуры",
        hasBlobs = true,
        hasNeonGlow = true
    ),
    CYBERPUNK_BLOOD(
        "Cyberpunk Blood & Gold",
        "Тёмный рубиновый обсидиан, горящие золотые и кроваво-красные огни",
        hasBlobs = true,
        hasNeonGlow = true
    ),
    RETRO_PIXEL(
        "Retro 8-Bit Matrix",
        "Тёмная кибер-сетка CRT, изумрудный терминал и ретро-игровая эстетика",
        hasBlobs = false,
        hasNeonGlow = true
    ),
    MATERIAL_YOU(
        "Material You Dynamic",
        "Адаптивные системные цвета Android 12+ без лишней подсветки",
        hasBlobs = false,
        hasNeonGlow = false
    )
}

enum class IconPackStyle(
    val displayName: String,
    val description: String
) {
    MINIMAL_THIN(
        "Ультратонкий минимализм",
        "Строгие тонкие линейные контуры толщиной 1dp, чистая геометрия"
    ),
    PIXEL_8BIT(
        "Пиксельный 8-Bit ретро",
        "Угловатые пиксельные ретро-иконки в стиле олдскульных консолей"
    ),
    BOLD_JUICY(
        "Жирные и сочные (Filled)",
        "Массивные, плотно залитые округлые иконки с максимальным акцентом"
    ),
    NEON_GLOW(
        "Неоновый контур (Cyber Glow)",
        "Двойной светящийся неоновый стиль с ярким градиентом"
    ),
    MATERIAL_ROUNDED(
        "Material 3 Toned",
        "Классические скругленные иконки Material You"
    )
}

enum class ProgressBarStyle(
    val displayName: String,
    val description: String
) {
    CLASSIC_LINE(
        "Классический тонкий слайдер",
        "Элегантный лаконичный бегунок с плавным скраббингом"
    ),
    WAVEFORM_BARS(
        "Звуковая волна (Sound Waveform)",
        "Живые анимированные частотные столбики в стиле SoundCloud и Spotify"
    ),
    LASER_GLOW(
        "Неоновый лазерный луч (Laser Pulse)",
        "Светящийся лазерный трек с бегущим световым импульсом"
    ),
    CHUNKY_PILL(
        "Стеклянный Pill-бар (Modern Thick)",
        "Современный объемный закруглённый брусок с эффектом стекла"
    )
}

enum class PlayerCoverStyle(
    val displayName: String,
    val description: String
) {
    ROUNDED_CARD(
        "Скругленная карточка (Modern Glass)",
        "Стильная парящая обложка с мягкими тенями"
    ),
    DYNAMIC_GLOW(
        "Глубокое сияние (Ambient Aura)",
        "Интенсивное неоновое свечение в такт играющей музыке"
    ),
    SPINNING_VINYL(
        "Виниловая пластинка (Retro Vinyl)",
        "Вращающаяся виниловая пластинка с обложкой по центру при игре"
    ),
    CYBER_CASSETTE(
        "Кассетный плеер (Retro Cassette)",
        "Аудиокассета с вращающимися катушками и магнитным окошком"
    )
}

enum class ScreenOffSkipMode(
    val displayName: String,
    val description: String
) {
    DISABLED(
        "Выключено",
        "Стандартное поведение экрана"
    ),
    VOLUME_BUTTONS_DOUBLE_CLICK(
        "Двойное нажатие громкости (+ / -)",
        "Громкость вверх 2x — следующий трек, Громкость вниз 2x — предыдущий"
    ),
    HEADSET_MULTI_CLICK(
        "Кнопки гарнитуры / наушников",
        "Двойной клик — следующий трек, Тройной клик — предыдущий"
    ),
    PROXIMITY_HOVER(
        "Датчик приближения (Поднесение руки)",
        "Удержание ладони над верхней частью телефона на 1.5 сек переключает трек"
    )
}

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("openfy_settings", Context.MODE_PRIVATE)

    private val _themeStyle = MutableStateFlow(AppThemeStyle.MIDNIGHT_NEON)
    val themeStyle: StateFlow<AppThemeStyle> = _themeStyle.asStateFlow()

    private val _customThemeId = MutableStateFlow<String?>(null)
    val customThemeId: StateFlow<String?> = _customThemeId.asStateFlow()

    private val _appLauncherIconKey = MutableStateFlow("MIDNIGHT_PLAY")
    val appLauncherIconKey: StateFlow<String> = _appLauncherIconKey.asStateFlow()

    private val _iconPackStyle = MutableStateFlow(IconPackStyle.MINIMAL_THIN)
    val iconPackStyle: StateFlow<IconPackStyle> = _iconPackStyle.asStateFlow()

    private val _progressBarStyle = MutableStateFlow(ProgressBarStyle.WAVEFORM_BARS)
    val progressBarStyle: StateFlow<ProgressBarStyle> = _progressBarStyle.asStateFlow()

    private val _playerCoverStyle = MutableStateFlow(PlayerCoverStyle.DYNAMIC_GLOW)
    val playerCoverStyle: StateFlow<PlayerCoverStyle> = _playerCoverStyle.asStateFlow()

    private val _screenOffSkipMode = MutableStateFlow(ScreenOffSkipMode.DISABLED)
    val screenOffSkipMode: StateFlow<ScreenOffSkipMode> = _screenOffSkipMode.asStateFlow()

    private val _smartSleepGuardEnabled = MutableStateFlow(true)
    val smartSleepGuardEnabled: StateFlow<Boolean> = _smartSleepGuardEnabled.asStateFlow()

    private val _smartSleepInactivityMinutes = MutableStateFlow(25)
    val smartSleepInactivityMinutes: StateFlow<Int> = _smartSleepInactivityMinutes.asStateFlow()

    private val _smartSleepNatureSound = MutableStateFlow(NatureSoundType.NIGHT_RAIN)
    val smartSleepNatureSound: StateFlow<NatureSoundType> = _smartSleepNatureSound.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _pauseOnUnplug = MutableStateFlow(true)
    val pauseOnUnplug: StateFlow<Boolean> = _pauseOnUnplug.asStateFlow()

    private val _gaplessPlayback = MutableStateFlow(true)
    val gaplessPlayback: StateFlow<Boolean> = _gaplessPlayback.asStateFlow()

    private val _fadeOnPlayPause = MutableStateFlow(true)
    val fadeOnPlayPause: StateFlow<Boolean> = _fadeOnPlayPause.asStateFlow()

    private val _ignoreShortTracks = MutableStateFlow(true)
    val ignoreShortTracks: StateFlow<Boolean> = _ignoreShortTracks.asStateFlow()

    private val _compactListMode = MutableStateFlow(false)
    val compactListMode: StateFlow<Boolean> = _compactListMode.asStateFlow()

    private val _seekIntervalSeconds = MutableStateFlow(10)
    val seekIntervalSeconds: StateFlow<Int> = _seekIntervalSeconds.asStateFlow()

    private val _carModeAutoLaunch = MutableStateFlow(false)
    val carModeAutoLaunch: StateFlow<Boolean> = _carModeAutoLaunch.asStateFlow()

    private val _carModeKeepScreenOn = MutableStateFlow(true)
    val carModeKeepScreenOn: StateFlow<Boolean> = _carModeKeepScreenOn.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val themeName = prefs.getString(KEY_THEME_STYLE, AppThemeStyle.MIDNIGHT_NEON.name)
        _themeStyle.value = try { AppThemeStyle.valueOf(themeName!!) } catch (e: Exception) { AppThemeStyle.MIDNIGHT_NEON }
        _customThemeId.value = prefs.getString(KEY_CUSTOM_THEME_ID, null)
        _appLauncherIconKey.value = prefs.getString(KEY_APP_LAUNCHER_ICON, "MIDNIGHT_PLAY") ?: "MIDNIGHT_PLAY"

        _iconPackStyle.value = IconPackStyle.MINIMAL_THIN

        val barName = prefs.getString(KEY_PROGRESS_BAR_STYLE, ProgressBarStyle.WAVEFORM_BARS.name)
        _progressBarStyle.value = try { ProgressBarStyle.valueOf(barName!!) } catch (e: Exception) { ProgressBarStyle.WAVEFORM_BARS }

        val coverName = prefs.getString(KEY_PLAYER_COVER_STYLE, PlayerCoverStyle.DYNAMIC_GLOW.name)
        _playerCoverStyle.value = try { PlayerCoverStyle.valueOf(coverName!!) } catch (e: Exception) { PlayerCoverStyle.DYNAMIC_GLOW }

        val skipName = prefs.getString(KEY_SCREEN_OFF_SKIP_MODE, ScreenOffSkipMode.DISABLED.name)
        _screenOffSkipMode.value = try { ScreenOffSkipMode.valueOf(skipName!!) } catch (e: Exception) { ScreenOffSkipMode.DISABLED }

        _smartSleepGuardEnabled.value = prefs.getBoolean(KEY_SMART_SLEEP_ENABLED, true)
        _smartSleepInactivityMinutes.value = prefs.getInt(KEY_SMART_SLEEP_MINUTES, 25)

        val soundName = prefs.getString(KEY_SMART_SLEEP_NATURE_SOUND, NatureSoundType.NIGHT_RAIN.name)
        _smartSleepNatureSound.value = try { NatureSoundType.valueOf(soundName!!) } catch (e: Exception) { NatureSoundType.NIGHT_RAIN }

        _isShuffle.value = prefs.getBoolean(KEY_SHUFFLE, false)
        val rModeName = prefs.getString(KEY_REPEAT_MODE, RepeatMode.OFF.name)
        _repeatMode.value = try { RepeatMode.valueOf(rModeName!!) } catch (e: Exception) { RepeatMode.OFF }

        _playbackSpeed.value = prefs.getFloat(KEY_SPEED, 1.0f)

        _pauseOnUnplug.value = prefs.getBoolean(KEY_PAUSE_ON_UNPLUG, true)
        _gaplessPlayback.value = prefs.getBoolean(KEY_GAPLESS_PLAYBACK, true)
        _fadeOnPlayPause.value = prefs.getBoolean(KEY_FADE_ON_PLAY_PAUSE, true)
        _ignoreShortTracks.value = prefs.getBoolean(KEY_IGNORE_SHORT_TRACKS, true)
        _compactListMode.value = prefs.getBoolean(KEY_COMPACT_LIST_MODE, false)
        _seekIntervalSeconds.value = prefs.getInt(KEY_SEEK_INTERVAL_SECONDS, 10)
        _carModeAutoLaunch.value = prefs.getBoolean(KEY_CAR_MODE_AUTO_LAUNCH, false)
        _carModeKeepScreenOn.value = prefs.getBoolean(KEY_CAR_MODE_KEEP_SCREEN_ON, true)
        _isOnboardingCompleted.value = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean = true) {
        _isOnboardingCompleted.value = completed
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun setCarModeAutoLaunch(enabled: Boolean) {
        _carModeAutoLaunch.value = enabled
        prefs.edit().putBoolean(KEY_CAR_MODE_AUTO_LAUNCH, enabled).apply()
    }

    fun setCarModeKeepScreenOn(enabled: Boolean) {
        _carModeKeepScreenOn.value = enabled
        prefs.edit().putBoolean(KEY_CAR_MODE_KEEP_SCREEN_ON, enabled).apply()
    }

    fun setThemeStyle(style: AppThemeStyle) {
        _themeStyle.value = style
        _customThemeId.value = null
        prefs.edit().putString(KEY_THEME_STYLE, style.name).remove(KEY_CUSTOM_THEME_ID).apply()
    }

    fun setCustomThemeId(themeId: String?) {
        _customThemeId.value = themeId
        if (themeId == null) {
            prefs.edit().remove(KEY_CUSTOM_THEME_ID).apply()
        } else {
            prefs.edit().putString(KEY_CUSTOM_THEME_ID, themeId).apply()
        }
    }

    fun setAppLauncherIconKey(key: String) {
        _appLauncherIconKey.value = key
        prefs.edit().putString(KEY_APP_LAUNCHER_ICON, key).apply()
    }

    fun setIconPackStyle(style: IconPackStyle) {
        _iconPackStyle.value = style
        prefs.edit().putString(KEY_ICON_PACK_STYLE, style.name).apply()
    }

    fun setProgressBarStyle(style: ProgressBarStyle) {
        _progressBarStyle.value = style
        prefs.edit().putString(KEY_PROGRESS_BAR_STYLE, style.name).apply()
    }

    fun setPlayerCoverStyle(style: PlayerCoverStyle) {
        _playerCoverStyle.value = style
        prefs.edit().putString(KEY_PLAYER_COVER_STYLE, style.name).apply()
    }

    fun setScreenOffSkipMode(mode: ScreenOffSkipMode) {
        _screenOffSkipMode.value = mode
        prefs.edit().putString(KEY_SCREEN_OFF_SKIP_MODE, mode.name).apply()
    }

    fun setSmartSleepGuardEnabled(enabled: Boolean) {
        _smartSleepGuardEnabled.value = enabled
        prefs.edit().putBoolean(KEY_SMART_SLEEP_ENABLED, enabled).apply()
    }

    fun setSmartSleepInactivityMinutes(minutes: Int) {
        _smartSleepInactivityMinutes.value = minutes
        prefs.edit().putInt(KEY_SMART_SLEEP_MINUTES, minutes).apply()
    }

    fun setSmartSleepNatureSound(sound: NatureSoundType) {
        _smartSleepNatureSound.value = sound
        prefs.edit().putString(KEY_SMART_SLEEP_NATURE_SOUND, sound.name).apply()
    }

    fun setIsShuffle(shuffle: Boolean) {
        _isShuffle.value = shuffle
        prefs.edit().putBoolean(KEY_SHUFFLE, shuffle).apply()
    }

    fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
        prefs.edit().putString(KEY_REPEAT_MODE, mode.name).apply()
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        prefs.edit().putFloat(KEY_SPEED, speed).apply()
    }

    fun setPauseOnUnplug(enabled: Boolean) {
        _pauseOnUnplug.value = enabled
        prefs.edit().putBoolean(KEY_PAUSE_ON_UNPLUG, enabled).apply()
    }

    fun setGaplessPlayback(enabled: Boolean) {
        _gaplessPlayback.value = enabled
        prefs.edit().putBoolean(KEY_GAPLESS_PLAYBACK, enabled).apply()
    }

    fun setFadeOnPlayPause(enabled: Boolean) {
        _fadeOnPlayPause.value = enabled
        prefs.edit().putBoolean(KEY_FADE_ON_PLAY_PAUSE, enabled).apply()
    }

    fun setIgnoreShortTracks(enabled: Boolean) {
        _ignoreShortTracks.value = enabled
        prefs.edit().putBoolean(KEY_IGNORE_SHORT_TRACKS, enabled).apply()
    }

    fun setCompactListMode(enabled: Boolean) {
        _compactListMode.value = enabled
        prefs.edit().putBoolean(KEY_COMPACT_LIST_MODE, enabled).apply()
    }

    fun setSeekIntervalSeconds(seconds: Int) {
        _seekIntervalSeconds.value = seconds
        prefs.edit().putInt(KEY_SEEK_INTERVAL_SECONDS, seconds).apply()
    }

    companion object {
        private const val KEY_THEME_STYLE = "theme_style"
        private const val KEY_CUSTOM_THEME_ID = "custom_theme_id"
        private const val KEY_APP_LAUNCHER_ICON = "key_app_launcher_icon"
        private const val KEY_ICON_PACK_STYLE = "icon_pack_style"
        private const val KEY_PROGRESS_BAR_STYLE = "progress_bar_style"
        private const val KEY_PLAYER_COVER_STYLE = "player_cover_style"
        private const val KEY_SCREEN_OFF_SKIP_MODE = "screen_off_skip_mode"
        private const val KEY_SMART_SLEEP_ENABLED = "smart_sleep_enabled"
        private const val KEY_SMART_SLEEP_MINUTES = "smart_sleep_minutes"
        private const val KEY_SMART_SLEEP_NATURE_SOUND = "smart_sleep_nature_sound"
        private const val KEY_SHUFFLE = "is_shuffle"
        private const val KEY_REPEAT_MODE = "repeat_mode"
        private const val KEY_SPEED = "playback_speed"
        private const val KEY_PAUSE_ON_UNPLUG = "pause_on_unplug"
        private const val KEY_GAPLESS_PLAYBACK = "gapless_playback"
        private const val KEY_FADE_ON_PLAY_PAUSE = "fade_on_play_pause"
        private const val KEY_IGNORE_SHORT_TRACKS = "ignore_short_tracks"
        private const val KEY_COMPACT_LIST_MODE = "compact_list_mode"
        private const val KEY_SEEK_INTERVAL_SECONDS = "seek_interval_seconds"
        private const val KEY_CAR_MODE_AUTO_LAUNCH = "car_mode_auto_launch"
        private const val KEY_CAR_MODE_KEEP_SCREEN_ON = "car_mode_keep_screen_on"
        private const val KEY_ONBOARDING_COMPLETED = "is_onboarding_completed"
    }
}
