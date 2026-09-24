package com.wxn.reader.util.tts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsPageSkipRequestGateTest {

    @Test
    fun `cancelling a request prevents it from resuming narration`() {
        val gate = TtsPageSkipRequestGate()
        val request = gate.begin()

        gate.cancel()

        assertFalse(gate.isCurrent(request))
    }

    @Test
    fun `a new request remains current after an earlier request is cancelled`() {
        val gate = TtsPageSkipRequestGate()
        val firstRequest = gate.begin()

        gate.cancel()
        val secondRequest = gate.begin()

        assertFalse(gate.isCurrent(firstRequest))
        assertTrue(gate.isCurrent(secondRequest))
    }
}
