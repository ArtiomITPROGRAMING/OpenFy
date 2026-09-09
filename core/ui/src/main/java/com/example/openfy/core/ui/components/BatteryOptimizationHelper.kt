package com.example.openfy.core.ui.components

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

enum class OemBrand(val displayName: String, val tip: String) {
    XIAOMI("Xiaomi / HyperOS / MIUI", "Разрешите «Автозапуск» и выберите режим энергосбережения «Нет ограничений» в Центре безопасности."),
    SAMSUNG("Samsung OneUI", "Добавьте OpenFy в «Никогда не спящие приложения» в настройках батареи Device Care."),
    HUAWEI("Huawei / Honor (EMUI)", "Включите «Ручное управление» (Автозапуск, Косвенный запуск, Работа в фоне) в Диспетчере телефона."),
    ONEPLUS_OPPO("OnePlus / Oppo / Realme", "Отключите оптимизацию батареи и разрешите работу в фоновом режиме в настройках приложения."),
    VIVO("Vivo / iQOO (FuntouchOS)", "Разрешите высокий расход батареи в фоне в настройках диспетчера."),
    GENERIC("Android / Pixel / AOSP", "Отключите ограничение фоновой активности для бесперебойного воспроизведения музыки.")
}

object BatteryOptimizationHelper {

    fun getDeviceBrand(): OemBrand {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()

        return when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") || brand.contains("poco") -> OemBrand.XIAOMI
            manufacturer.contains("samsung") -> OemBrand.SAMSUNG
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> OemBrand.HUAWEI
            manufacturer.contains("oneplus") || manufacturer.contains("oppo") || manufacturer.contains("realme") -> OemBrand.ONEPLUS_OPPO
            manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> OemBrand.VIVO
            else -> OemBrand.GENERIC
        }
    }

    /**
     * Returns true if battery optimizations are already disabled for OpenFy.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Requests battery optimization ignore or opens OEM auto-start management settings.
     */
    @SuppressLint("BatteryLife")
    fun requestDisableBatteryOptimization(context: Context): Boolean {
        // 1. Try standard ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return true
            }
        } catch (_: Exception) {}

        // 2. Fallback to generic ignore battery optimization settings
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return true
            }
        } catch (_: Exception) {}

        // 3. Fallback to app details
        return openAppDetailsSettings(context)
    }

    /**
     * Opens OEM-specific Auto-Start / Background management screen.
     */
    fun openOemAutoStartSettings(context: Context): Boolean {
        val oemIntents = listOf(
            // Xiaomi / MIUI / HyperOS
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings")),

            // Samsung
            Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            Intent().setComponent(ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity")),

            // Huawei / Honor
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),

            // OnePlus / Oppo / ColorOS
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.oplus.safecenter", "com.oplus.safecenter.permission.startup.StartupAppListActivity")),

            // Vivo
            Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"))
        )

        for (intent in oemIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return true
                }
            } catch (_: Exception) {}
        }

        // Fallback to standard App Details
        return openAppDetailsSettings(context)
    }

    fun openAppDetailsSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
