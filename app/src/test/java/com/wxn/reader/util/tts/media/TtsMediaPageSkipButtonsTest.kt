package com.wxn.reader.util.tts.media

import androidx.media3.common.Player
import androidx.media3.session.CommandButton
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TtsMediaPageSkipButtonsTest {

    @Test
    fun `provides custom page commands in the backward and forward slots`() {
        val buttons = TtsMediaPageSkipButtons.create(
            backwardLabel = "Previous page",
            forwardLabel = "Next page",
        )

        assertEquals(2, buttons.size)
        assertEquals(
            "com.wxn.reader.tts.media.PREVIOUS_PAGE",
            buttons[0].sessionCommand?.customAction,
        )
        assertEquals(Player.COMMAND_INVALID, buttons[0].playerCommand)
        assertEquals(CommandButton.ICON_REWIND, buttons[0].icon)
        assertEquals(CommandButton.SLOT_BACK, buttons[0].slots[0])
        assertEquals(
            "com.wxn.reader.tts.media.NEXT_PAGE",
            buttons[1].sessionCommand?.customAction,
        )
        assertEquals(Player.COMMAND_INVALID, buttons[1].playerCommand)
        assertEquals(CommandButton.ICON_FAST_FORWARD, buttons[1].icon)
        assertEquals(CommandButton.SLOT_FORWARD, buttons[1].slots[0])
    }
}
