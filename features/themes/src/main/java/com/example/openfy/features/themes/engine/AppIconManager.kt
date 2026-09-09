package com.example.openfy.features.themes.engine

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

enum class AppLauncherIcon(
    val key: String,
    val title: String,
    val aliasSuffix: String,
    val description: String,
    val primaryColorHex: String
) {
    MIDNIGHT_PLAY(
        key = "MIDNIGHT_PLAY",
        title = "Midnight Play",
        aliasSuffix = "MainActivityMidnight",
        description = "Темный матовый фон с фиолетово-неоновым незамкнутым кольцом и символом Play",
        primaryColorHex = "#C084FC"
    ),
    NEON_CORE(
        key = "NEON_CORE",
        title = "Neon Core",
        aliasSuffix = "MainActivityNeon",
        description = "Фирменный неоновый стиль Cyan & Electric Pink с двойным кольцом",
        primaryColorHex = "#00E5FF"
    ),
    TITANIUM_STUDIO(
        key = "TITANIUM_STUDIO",
        title = "Titanium Studio",
        aliasSuffix = "MainActivityTitanium",
        description = "Матовый титан, полированная сталь и лаконичная геометрия",
        primaryColorHex = "#FFFFFF"
    ),
    SPECTRUM_WAVE(
        key = "SPECTRUM_WAVE",
        title = "Spectrum Wave",
        aliasSuffix = "MainActivitySpectrum",
        description = "Круговой эквалайзер с аудио-лучами и спектральным импульсом",
        primaryColorHex = "#FF0077"
    );

    companion object {
        val DEFAULT = MIDNIGHT_PLAY

        fun fromKey(key: String?): AppLauncherIcon {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) }
                ?: DEFAULT
        }
    }
}

object AppIconManager {

    /**
     * Dynamically switches the active Android launcher icon via [PackageManager.setComponentEnabledSetting].
     */
    fun setAppIcon(context: Context, targetIcon: AppLauncherIcon) {
        val packageManager = context.packageManager
        val packageName = context.packageName

        AppLauncherIcon.entries.forEach { icon ->
            val component = ComponentName(packageName, "$packageName.${icon.aliasSuffix}")
            val newState = if (icon == targetIcon) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            try {
                packageManager.setComponentEnabledSetting(
                    component,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            } catch (_: Exception) {}
        }
    }

    /**
     * Resolves the currently active launcher icon from PackageManager.
     */
    fun getActiveAppIcon(context: Context): AppLauncherIcon {
        val packageManager = context.packageManager
        val packageName = context.packageName

        for (icon in AppLauncherIcon.entries) {
            val component = ComponentName(packageName, "$packageName.${icon.aliasSuffix}")
            val state = try {
                packageManager.getComponentEnabledSetting(component)
            } catch (_: Exception) {
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
            }
            if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return icon
            }
        }
        return AppLauncherIcon.DEFAULT
    }

    /**
     * Ensures at least one valid launcher icon alias is enabled on app launch.
     */
    fun ensureAppIconEnabled(context: Context, targetIcon: AppLauncherIcon = AppLauncherIcon.DEFAULT) {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val targetComponent = ComponentName(packageName, "$packageName.${targetIcon.aliasSuffix}")
        val state = try {
            packageManager.getComponentEnabledSetting(targetComponent)
        } catch (_: Exception) {
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        }
        if (state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            setAppIcon(context, targetIcon)
        }
    }
}
