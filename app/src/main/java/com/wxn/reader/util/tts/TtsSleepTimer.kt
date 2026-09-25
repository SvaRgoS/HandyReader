package com.wxn.reader.util.tts

import java.util.concurrent.TimeUnit
import kotlin.math.max

enum class TtsSleepTimerOption(val durationMillis: Long?) {
    Off(null),
    Minutes30(TimeUnit.MINUTES.toMillis(30)),
    Hour1(TimeUnit.HOURS.toMillis(1)),
    Hours2(TimeUnit.HOURS.toMillis(2)),
}

class TtsSleepTimer(
    private val nowMillis: () -> Long,
) {
    private var expiryMillis: Long? = null

    fun start(durationMillis: Long) {
        require(durationMillis > 0) { "Sleep timer duration must be positive" }
        expiryMillis = nowMillis() + durationMillis
    }

    fun cancel() {
        expiryMillis = null
    }

    fun remainingMillis(): Long? = expiryMillis?.let { expiry ->
        max(0L, expiry - nowMillis())
    }

    fun consumeExpiry(): Boolean {
        val expiry = expiryMillis ?: return false
        if (nowMillis() < expiry) {
            return false
        }
        expiryMillis = null
        return true
    }
}
