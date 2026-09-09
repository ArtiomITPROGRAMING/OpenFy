package com.example.openfy.core.audio.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.UiModeManager
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.example.openfy.core.audio.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CarModeManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val settingsRepository = SettingsRepository(context)

    private val _isCarModeActive = MutableStateFlow(false)
    val isCarModeActive: StateFlow<Boolean> = _isCarModeActive.asStateFlow()

    private val _isBluetoothCarConnected = MutableStateFlow(false)
    val isBluetoothCarConnected: StateFlow<Boolean> = _isBluetoothCarConnected.asStateFlow()

    private val carReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action) {
                UiModeManager.ACTION_ENTER_CAR_MODE -> {
                    setCarModeActive(true)
                }
                UiModeManager.ACTION_EXIT_CAR_MODE -> {
                    setCarModeActive(false)
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    checkBluetoothCarDevice(intent)
                }
                BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED)
                    if (state == BluetoothA2dp.STATE_CONNECTED) {
                        checkBluetoothCarDevice(intent)
                    } else if (state == BluetoothA2dp.STATE_DISCONNECTED) {
                        _isBluetoothCarConnected.value = false
                    }
                }
            }
        }
    }

    init {
        checkSystemCarMode()
        registerReceiver()
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(UiModeManager.ACTION_ENTER_CAR_MODE)
            addAction(UiModeManager.ACTION_EXIT_CAR_MODE)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(carReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(carReceiver, filter)
            }
        } catch (e: Exception) {
            // Receiver registration fallback
        }
    }

    private fun checkSystemCarMode() {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        if (uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_CAR) {
            _isCarModeActive.value = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkBluetoothCarDevice(intent: Intent) {
        val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val deviceClass = if (hasPermission) {
            try {
                device?.bluetoothClass?.deviceClass
            } catch (e: SecurityException) {
                null
            }
        } else {
            null
        }

        val isCarAudio = deviceClass == 1032 || deviceClass == 1056 // Audio video car audio / handsfree

        if (isCarAudio) {
            _isBluetoothCarConnected.value = true
            if (settingsRepository.carModeAutoLaunch.value) {
                setCarModeActive(true)
            }
        }
    }

    fun setCarModeActive(active: Boolean) {
        _isCarModeActive.value = active
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: CarModeManager? = null

        fun getInstance(context: Context): CarModeManager {
            return instance ?: synchronized(this) {
                instance ?: CarModeManager(context.applicationContext).also { instance = it }
            }
        }

        fun keepScreenOn(window: Window?, enable: Boolean) {
            window?.let {
                if (enable) {
                    it.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    it.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }
    }
}
