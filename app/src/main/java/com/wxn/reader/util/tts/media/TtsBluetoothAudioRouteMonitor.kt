package com.wxn.reader.util.tts.media

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Reports whether an output suitable for Bluetooth media controls is connected. */
internal class TtsBluetoothAudioRouteMonitor(context: Context) {
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val _isBluetoothOutputConnected = MutableStateFlow(currentBluetoothOutputConnected())

    val isBluetoothOutputConnected: StateFlow<Boolean> = _isBluetoothOutputConnected.asStateFlow()

    private var isStarted = false
    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
            refresh()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
            refresh()
        }
    }

    fun start() {
        if (isStarted) {
            return
        }
        isStarted = true
        refresh()
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
    }

    fun stop() {
        if (!isStarted) {
            return
        }
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        isStarted = false
    }

    private fun refresh() {
        _isBluetoothOutputConnected.value = currentBluetoothOutputConnected()
    }

    private fun currentBluetoothOutputConnected(): Boolean = hasBluetoothAudioOutput(
        audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).map(AudioDeviceInfo::getType),
    )
}

internal fun hasBluetoothAudioOutput(deviceTypes: Iterable<Int>): Boolean = deviceTypes.any { type ->
    when (type) {
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
        AudioDeviceInfo.TYPE_HEARING_AID,
        -> true

        else -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            (type == AudioDeviceInfo.TYPE_BLE_HEADSET || type == AudioDeviceInfo.TYPE_BLE_SPEAKER)
    }
}
