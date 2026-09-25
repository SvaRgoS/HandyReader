package com.wxn.reader.util.tts.media

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.wxn.base.util.Logger

/**
 * Keeps Android's media-button routing associated with this process while a platform TTS engine
 * owns the audible [AudioTrack]. The generated track contains only zero PCM samples.
 */
class TtsMediaButtonAudioAnchor(
    private val output: TtsMediaButtonAudioOutput,
) {
    private var isActive = false

    fun update(
        playbackState: TtsMediaPlaybackState,
        isBluetoothOutputConnected: Boolean = true,
    ) {
        val shouldBeActive =
            playbackState != TtsMediaPlaybackState.Idle && isBluetoothOutputConnected
        if (shouldBeActive == isActive) {
            return
        }
        if (shouldBeActive) {
            isActive = output.start()
        } else {
            output.stop()
            isActive = false
        }
    }

    fun release() {
        if (isActive) {
            output.stop()
            isActive = false
        }
    }
}

interface TtsMediaButtonAudioOutput {
    fun start(): Boolean

    fun stop()
}

internal class AndroidTtsMediaButtonAudioOutput(
    private val createTrack: () -> TtsMediaButtonPlatformAudioTrack = ::createPlatformAudioTrack,
) : TtsMediaButtonAudioOutput {
    private var audioTrack: TtsMediaButtonPlatformAudioTrack? = null

    override fun start(): Boolean {
        if (audioTrack != null) {
            return true
        }
        var track: TtsMediaButtonPlatformAudioTrack? = null
        return try {
            track = createTrack()
            val trackState = track.state
            check(
                trackState == AudioTrack.STATE_INITIALIZED ||
                    trackState == AudioTrack.STATE_NO_STATIC_DATA,
            ) {
                "AudioTrack initialization returned state=$trackState"
            }
            val writeResult = track.write(
                SILENCE_BYTES,
                0,
                SILENCE_BYTES.size,
                AudioTrack.WRITE_BLOCKING,
            )
            check(writeResult == SILENCE_BYTES.size) {
                "AudioTrack write returned $writeResult"
            }
            val loopResult = track.setLoopPoints(0, FRAME_COUNT, LOOP_FOREVER)
            check(loopResult == AudioTrack.SUCCESS) {
                "AudioTrack loop configuration returned $loopResult"
            }
            track.play()
            audioTrack = track
            true
        } catch (error: Exception) {
            track?.release()
            Logger.e("AndroidTtsMediaButtonAudioOutput::start failed: $error")
            false
        }
    }

    override fun stop() {
        audioTrack?.release()
        audioTrack = null
    }

    private companion object {
        const val SAMPLE_RATE_HZ = 8_000
        const val FRAME_COUNT = SAMPLE_RATE_HZ
        const val LOOP_FOREVER = -1
        val SILENCE_BYTES = ByteArray(FRAME_COUNT * Short.SIZE_BYTES)
    }
}

internal interface TtsMediaButtonPlatformAudioTrack {
    val state: Int

    fun write(silence: ByteArray, offsetInBytes: Int, sizeInBytes: Int, writeMode: Int): Int

    fun setLoopPoints(startFrame: Int, endFrame: Int, loopCount: Int): Int

    fun play()

    fun release()
}

private fun createPlatformAudioTrack(): TtsMediaButtonPlatformAudioTrack {
    val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(8_000)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
        )
        .setBufferSizeInBytes(8_000 * Short.SIZE_BYTES)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()
    return AndroidTtsMediaButtonPlatformAudioTrack(audioTrack)
}

private class AndroidTtsMediaButtonPlatformAudioTrack(
    private val audioTrack: AudioTrack,
) : TtsMediaButtonPlatformAudioTrack {
    override val state: Int
        get() = audioTrack.state

    override fun write(
        silence: ByteArray,
        offsetInBytes: Int,
        sizeInBytes: Int,
        writeMode: Int,
    ): Int = audioTrack.write(silence, offsetInBytes, sizeInBytes, writeMode)

    override fun setLoopPoints(startFrame: Int, endFrame: Int, loopCount: Int): Int =
        audioTrack.setLoopPoints(startFrame, endFrame, loopCount)

    override fun play() {
        audioTrack.play()
    }

    override fun release() {
        audioTrack.release()
    }
}
