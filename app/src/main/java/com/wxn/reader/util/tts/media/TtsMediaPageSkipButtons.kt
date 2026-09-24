package com.wxn.reader.util.tts.media

import android.os.Bundle
import androidx.media3.session.CommandButton
import androidx.media3.session.SessionCommand

object TtsMediaPageSkipButtons {
    private const val PREVIOUS_PAGE_ACTION = "com.wxn.reader.tts.media.PREVIOUS_PAGE"
    private const val NEXT_PAGE_ACTION = "com.wxn.reader.tts.media.NEXT_PAGE"

    val previousPageCommand = SessionCommand(PREVIOUS_PAGE_ACTION, Bundle.EMPTY)
    val nextPageCommand = SessionCommand(NEXT_PAGE_ACTION, Bundle.EMPTY)

    fun create(
        backwardLabel: String,
        forwardLabel: String,
    ): List<CommandButton> = listOf(
        CommandButton.Builder(CommandButton.ICON_REWIND)
            .setSessionCommand(previousPageCommand)
            .setDisplayName(backwardLabel)
            .setSlots(CommandButton.SLOT_BACK)
            .build(),
        CommandButton.Builder(CommandButton.ICON_FAST_FORWARD)
            .setSessionCommand(nextPageCommand)
            .setDisplayName(forwardLabel)
            .setSlots(CommandButton.SLOT_FORWARD)
            .build(),
    )
}
