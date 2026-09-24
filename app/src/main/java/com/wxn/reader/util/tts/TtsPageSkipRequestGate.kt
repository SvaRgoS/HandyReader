package com.wxn.reader.util.tts

/**
 * Invalidates in-flight page-skip requests when a user explicitly stops or pauses narration.
 */
class TtsPageSkipRequestGate {
    private var generation = 0L

    @Synchronized
    fun begin(): Long = generation

    @Synchronized
    fun cancel() {
        generation += 1
    }

    @Synchronized
    fun isCurrent(requestGeneration: Long): Boolean = requestGeneration == generation
}
