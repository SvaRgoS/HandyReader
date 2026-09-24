package com.wxn.reader.util.tts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TtsPageSkipTest {

    @Test
    fun `moves to an adjacent page when the target remains in the chapter`() {
        assertEquals(
            TtsPageSkipTarget.Page(pageIndex = 1),
            TtsPageSkip.target(
                currentPageIndex = 2,
                pageCount = 4,
                direction = TtsPageSkipDirection.Backward,
            ),
        )
        assertEquals(
            TtsPageSkipTarget.Page(pageIndex = 3),
            TtsPageSkip.target(
                currentPageIndex = 2,
                pageCount = 4,
                direction = TtsPageSkipDirection.Forward,
            ),
        )
    }

    @Test
    fun `requests the adjacent chapter at a page boundary`() {
        assertEquals(
            TtsPageSkipTarget.PreviousChapter,
            TtsPageSkip.target(
                currentPageIndex = 0,
                pageCount = 4,
                direction = TtsPageSkipDirection.Backward,
            ),
        )
        assertEquals(
            TtsPageSkipTarget.NextChapter,
            TtsPageSkip.target(
                currentPageIndex = 3,
                pageCount = 4,
                direction = TtsPageSkipDirection.Forward,
            ),
        )
    }

    @Test
    fun `does not produce a target for an empty chapter`() {
        assertNull(
            TtsPageSkip.target(
                currentPageIndex = 0,
                pageCount = 0,
                direction = TtsPageSkipDirection.Forward,
            ),
        )
    }
}
