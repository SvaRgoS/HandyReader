package com.wxn.reader.util.tts.media

import android.media.AudioTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsMediaButtonAudioAnchorTest {

    @Test
    fun `keeps Android audio routing active while narration is paused and releases it when stopped`() {
        val output = RecordingOutput()
        val anchor = TtsMediaButtonAudioAnchor(output)

        anchor.update(TtsMediaPlaybackState.Playing)
        anchor.update(TtsMediaPlaybackState.Paused)
        anchor.update(TtsMediaPlaybackState.Idle)

        assertEquals(listOf(AudioEvent.Start, AudioEvent.Stop), output.events)
    }

    @Test
    fun `does not recreate the routing audio while narration stays active`() {
        val output = RecordingOutput()
        val anchor = TtsMediaButtonAudioAnchor(output)

        anchor.update(TtsMediaPlaybackState.Playing)
        anchor.update(TtsMediaPlaybackState.Paused)
        anchor.update(TtsMediaPlaybackState.Playing)

        assertEquals(listOf(AudioEvent.Start), output.events)
    }

    @Test
    fun `keeps routing audio only while a Bluetooth output is connected`() {
        val output = RecordingOutput()
        val anchor = TtsMediaButtonAudioAnchor(output)

        anchor.update(TtsMediaPlaybackState.Playing, isBluetoothOutputConnected = false)
        anchor.update(TtsMediaPlaybackState.Playing, isBluetoothOutputConnected = true)
        anchor.update(TtsMediaPlaybackState.Paused, isBluetoothOutputConnected = true)
        anchor.update(TtsMediaPlaybackState.Paused, isBluetoothOutputConnected = false)

        assertEquals(listOf(AudioEvent.Start, AudioEvent.Stop), output.events)
    }

    @Test
    fun `releases routing audio when the media session is destroyed`() {
        val output = RecordingOutput()
        val anchor = TtsMediaButtonAudioAnchor(output)

        anchor.update(TtsMediaPlaybackState.Paused)
        anchor.release()

        assertEquals(listOf(AudioEvent.Start, AudioEvent.Stop), output.events)
    }

    @Test
    fun `retries the routing audio after Android rejects a start`() {
        val output = RecordingOutput(startResults = mutableListOf(false, true))
        val anchor = TtsMediaButtonAudioAnchor(output)

        anchor.update(TtsMediaPlaybackState.Playing)
        anchor.update(TtsMediaPlaybackState.Paused)

        assertEquals(listOf(AudioEvent.Start, AudioEvent.Start), output.events)
    }

    @Test
    fun `releases a native track when loop configuration fails`() {
        val track = FakePlatformAudioTrack(loopResult = -1)
        val output = AndroidTtsMediaButtonAudioOutput { track }

        assertFalse(output.start())
        assertEquals(1, track.releaseCount)
    }

    @Test
    fun `writes and plays a static track that has no data yet`() {
        val track = FakePlatformAudioTrack(
            loopResult = AudioTrack.SUCCESS,
            state = AudioTrack.STATE_NO_STATIC_DATA,
        )
        val output = AndroidTtsMediaButtonAudioOutput { track }

        assertTrue(output.start())
        assertEquals(1, track.writeCount)
        assertEquals(1, track.playCount)
        assertEquals(0, track.releaseCount)
    }

    private class RecordingOutput(
        private val startResults: MutableList<Boolean> = mutableListOf(true),
    ) : TtsMediaButtonAudioOutput {
        val events = mutableListOf<AudioEvent>()

        override fun start(): Boolean {
            events += AudioEvent.Start
            return startResults.removeFirstOrNull() ?: true
        }

        override fun stop() {
            events += AudioEvent.Stop
        }
    }

    private enum class AudioEvent {
        Start,
        Stop,
    }

    private class FakePlatformAudioTrack(
        private val loopResult: Int,
        override val state: Int = AudioTrack.STATE_INITIALIZED,
    ) : TtsMediaButtonPlatformAudioTrack {
        var releaseCount = 0
        var writeCount = 0
        var playCount = 0

        override fun write(
            silence: ByteArray,
            offsetInBytes: Int,
            sizeInBytes: Int,
            writeMode: Int,
        ): Int {
            writeCount += 1
            return sizeInBytes
        }

        override fun setLoopPoints(startFrame: Int, endFrame: Int, loopCount: Int): Int = loopResult

        override fun play() {
            playCount += 1
        }

        override fun release() {
            releaseCount += 1
        }
    }
}
