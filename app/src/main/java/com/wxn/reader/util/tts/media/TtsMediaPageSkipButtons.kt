package com.wxn.reader.util.tts.media

import androidx.media3.common.Player
import androidx.media3.session.CommandButton

object TtsMediaPageSkipButtons {
    fun create(
        backwardLabel: String,
        forwardLabel: String,
    ): List<CommandButton> = listOf(
        CommandButton.Builder(CommandButton.ICON_REWIND)
            .setPlayerCommand(Player.COMMAND_SEEK_BACK)
            .setDisplayName(backwardLabel)
            .setSlots(CommandButton.SLOT_BACK)
            .build(),
        CommandButton.Builder(CommandButton.ICON_FAST_FORWARD)
            .setPlayerCommand(Player.COMMAND_SEEK_FORWARD)
            .setDisplayName(forwardLabel)
            .setSlots(CommandButton.SLOT_FORWARD)
            .build(),
    )
}
