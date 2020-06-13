package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable
import java.util.concurrent.TimeUnit

class VibrateAction(data: Map<String, String>) : BaseAction() {

    private val patternId = data[KEY_PATTERN]?.toIntOrNull() ?: 0

    private val waitForCompletion = data[KEY_WAIT_FOR_COMPLETION]?.toBoolean() ?: false

    private val pattern: VibrationPattern
        get() = findPattern(patternId)

    override fun execute(executionContext: ExecutionContext): Completable {
        val vibrator = executionContext.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (!vibrator.hasVibrator()) {
            return Completable.complete()
        }
        val pattern = pattern
        pattern.execute(vibrator)
        return Completable.complete()
            .mapIf(waitForCompletion) {
                it.delay(pattern.duration, TimeUnit.MILLISECONDS)
            }
    }

    interface VibrationPattern {

        val duration: Long

        fun execute(vibrator: Vibrator)

    }

    companion object {

        const val KEY_PATTERN = "pattern"
        const val KEY_WAIT_FOR_COMPLETION = "wait"

        private fun findPattern(patternId: Int): VibrationPattern =
            when (patternId) {
                1 -> object : VibrationPattern {
                    override val duration: Long
                        get() = 1000L

                    override fun execute(vibrator: Vibrator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(duration)
                        }
                    }
                }
                2 -> object : VibrationPattern {
                    override val duration: Long
                        get() = 1200L

                    override fun execute(vibrator: Vibrator) {
                        val pattern = longArrayOf(200L, 200L, 200L, 200L, 200L, 200L)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(pattern, -1)
                        }
                    }
                }
                else -> object : VibrationPattern {
                    override val duration: Long
                        get() = 300L

                    override fun execute(vibrator: Vibrator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(duration)
                        }
                    }
                }
            }

    }

}