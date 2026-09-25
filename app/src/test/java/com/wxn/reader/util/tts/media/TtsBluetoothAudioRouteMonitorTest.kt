package com.wxn.reader.util.tts.media

import android.media.AudioDeviceInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsBluetoothAudioRouteMonitorTest {

    @Test
    fun `recognizes Bluetooth media and call outputs`() {
        assertTrue(
            hasBluetoothAudioOutput(
                listOf(
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                ),
            ),
        )
        assertTrue(hasBluetoothAudioOutput(listOf(AudioDeviceInfo.TYPE_BLUETOOTH_SCO)))
    }

    @Test
    fun `does not recognize built in and wired outputs as Bluetooth`() {
        assertFalse(
            hasBluetoothAudioOutput(
                listOf(
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                ),
            ),
        )
    }
}
