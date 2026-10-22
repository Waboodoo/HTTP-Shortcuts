package ch.rmy.android.http_shortcuts.media_controls

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import ch.rmy.android.framework.extensions.tryOrLog

internal class SilentAudioPlayer {

    private var audioTrack: AudioTrack? = null

    val audioAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    fun start() {
        if (audioTrack != null) {
            return
        }
        audioTrack = tryOrLog {
            val track = createAudioTrack()
            try {
                val silence = ShortArray(SILENCE_SAMPLE_COUNT)
                check(track.write(silence, 0, silence.size) == silence.size)
                check(track.setLoopPoints(0, silence.size, -1) == AudioTrack.SUCCESS)
                track.play()
                track
            } catch (e: Throwable) {
                track.release()
                throw e
            }
        }
    }

    fun release() {
        audioTrack?.let { track ->
            tryOrLog {
                try {
                    track.stop()
                } finally {
                    track.release()
                }
            }
        }
        audioTrack = null
    }

    private fun createAudioTrack() = AudioTrack.Builder()
        .setAudioAttributes(audioAttributes)
        .setAudioFormat(
            AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
        )
        .setBufferSizeInBytes(SILENCE_SAMPLE_COUNT * Short.SIZE_BYTES)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

    companion object {
        private const val SAMPLE_RATE = 8_000
        private const val SILENCE_SAMPLE_COUNT = SAMPLE_RATE / 10
    }
}
