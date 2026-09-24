package com.wxn.reader.util.tts.media

import androidx.media3.common.Player
import androidx.media3.session.CommandButton
import org.junit.Assert.assertEquals
import org.junit.Test

class TtsMediaPageSkipButtonsTest {

    @Test
    fun `provides standard seek buttons in the backward and forward slots`() {
        val buttons = TtsMediaPageSkipButtons.create(
            backwardLabel = "Previous page",
            forwardLabel = "Next page",
        )

        assertEquals(2, buttons.size)
        assertEquals(Player.COMMAND_SEEK_BACK, buttons[0].playerCommand)
        assertEquals(CommandButton.ICON_REWIND, buttons[0].icon)
        assertEquals(CommandButton.SLOT_BACK, buttons[0].slots[0])
        assertEquals(Player.COMMAND_SEEK_FORWARD, buttons[1].playerCommand)
        assertEquals(CommandButton.ICON_FAST_FORWARD, buttons[1].icon)
        assertEquals(CommandButton.SLOT_FORWARD, buttons[1].slots[0])
    }
}
