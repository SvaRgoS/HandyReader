package com.wxn.reader.util.tts.media

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TtsMediaButtonMapperTest {

    @Test
    fun `maps headset play pause keys using the current narration state`() {
        assertEquals(
            TtsMediaCommand.Pause,
            TtsMediaButtonMapper.commandFor(
                keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                isPlaying = true,
            ),
        )
        assertEquals(
            TtsMediaCommand.Play,
            TtsMediaButtonMapper.commandFor(
                keyCode = KeyEvent.KEYCODE_HEADSETHOOK,
                isPlaying = false,
            ),
        )
    }

    @Test
    fun `maps dedicated headset playback keys to narration commands`() {
        assertEquals(
            TtsMediaCommand.Play,
            TtsMediaButtonMapper.commandFor(
                keyCode = KeyEvent.KEYCODE_MEDIA_PLAY,
                isPlaying = false,
            ),
        )
        assertEquals(
            TtsMediaCommand.Pause,
            TtsMediaButtonMapper.commandFor(
                keyCode = KeyEvent.KEYCODE_MEDIA_PAUSE,
                isPlaying = true,
            ),
        )
        assertEquals(
            TtsMediaCommand.Stop,
            TtsMediaButtonMapper.commandFor(
                keyCode = KeyEvent.KEYCODE_MEDIA_STOP,
                isPlaying = true,
            ),
        )
    }

    @Test
    fun `maps headset navigation and seek keys to text page commands`() {
        assertEquals(
            TtsMediaCommand.SkipBackward,
            TtsMediaButtonMapper.commandFor(
                keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                isPlaying = true,
            ),
        )
        assertEquals(
            TtsMediaCommand.SkipBackward,
            TtsMediaButtonMapper.commandFor(
                keyCode = KeyEvent.KEYCODE_MEDIA_REWIND,
                isPlaying = true,
            ),
        )
        assertEquals(
            TtsMediaCommand.SkipForward,
            TtsMediaButtonMapper.commandFor(
                keyCode = KeyEvent.KEYCODE_MEDIA_NEXT,
                isPlaying = true,
            ),
        )
        assertEquals(
            TtsMediaCommand.SkipForward,
            TtsMediaButtonMapper.commandFor(
                keyCode = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
                isPlaying = true,
            ),
        )
        assertNull(
            TtsMediaButtonMapper.commandFor(
                keyCode = KeyEvent.KEYCODE_MEDIA_EJECT,
                isPlaying = true,
            ),
        )
    }
}
