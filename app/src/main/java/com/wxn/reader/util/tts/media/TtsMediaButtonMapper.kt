package com.wxn.reader.util.tts.media

import android.view.KeyEvent

object TtsMediaButtonMapper {
    fun commandFor(
        keyCode: Int,
        isPlaying: Boolean,
    ): TtsMediaCommand? {
        return when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY -> TtsMediaCommand.Play
            KeyEvent.KEYCODE_MEDIA_PAUSE -> TtsMediaCommand.Pause
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_HEADSETHOOK,
            -> if (isPlaying) TtsMediaCommand.Pause else TtsMediaCommand.Play
            KeyEvent.KEYCODE_MEDIA_STOP -> TtsMediaCommand.Stop
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD,
            KeyEvent.KEYCODE_MEDIA_REWIND,
            -> TtsMediaCommand.SkipBackward
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD,
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            -> TtsMediaCommand.SkipForward
            else -> null
        }
    }
}
