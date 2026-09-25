package com.wxn.reader.util.tts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class TtsSleepTimerTest {

    @Test
    fun `expires once after the selected listening duration`() {
        var nowMs = 10_000L
        val timer = TtsSleepTimer { nowMs }

        timer.start(TimeUnit.MINUTES.toMillis(30))

        assertEquals(TimeUnit.MINUTES.toMillis(30), timer.remainingMillis())
        nowMs += TimeUnit.MINUTES.toMillis(29) + TimeUnit.SECONDS.toMillis(59)
        assertEquals(TimeUnit.SECONDS.toMillis(1), timer.remainingMillis())
        assertFalse(timer.consumeExpiry())

        nowMs += TimeUnit.SECONDS.toMillis(1)

        assertTrue(timer.consumeExpiry())
        assertFalse(timer.consumeExpiry())
        assertNull(timer.remainingMillis())
    }

    @Test
    fun `cancelling a timer clears its remaining duration`() {
        val timer = TtsSleepTimer { 10_000L }

        timer.start(TimeUnit.HOURS.toMillis(2))
        timer.cancel()

        assertNull(timer.remainingMillis())
        assertFalse(timer.consumeExpiry())
    }
}
