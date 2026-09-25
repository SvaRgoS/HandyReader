package com.wxn.reader.presentation.home.components

import org.junit.Assert.assertEquals
import org.junit.Test

class TwoStageBookListPrefetchTest {

    @Test
    fun `forward scroll prepares the nearest and one additional card`() {
        assertEquals(
            listOf(8, 9),
            twoStageBookListPrefetchTargets(
                scrollDelta = -24f,
                visibleItemIndices = listOf(4, 5, 6, 7),
                totalItemCount = 20,
            ),
        )
    }

    @Test
    fun `backward scroll prepares the nearest and one additional card`() {
        assertEquals(
            listOf(3, 2),
            twoStageBookListPrefetchTargets(
                scrollDelta = 24f,
                visibleItemIndices = listOf(4, 5, 6, 7),
                totalItemCount = 20,
            ),
        )
    }

    @Test
    fun `preparation stops at the list edge`() {
        assertEquals(
            listOf(19),
            twoStageBookListPrefetchTargets(
                scrollDelta = -24f,
                visibleItemIndices = listOf(17, 18),
                totalItemCount = 20,
            ),
        )
    }
}
