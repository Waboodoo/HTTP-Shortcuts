package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import ch.rmy.android.http_shortcuts.extensions.userError
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.VibrationUtil
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class VibrateAction
@Inject
constructor(
    private val vibrationUtil: VibrationUtil,
) : Action<VibrateAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        val vibrator = vibrationUtil.getVibrator()
            ?: return

        val pattern = findPattern(patternName)
        withContext(Dispatchers.Main) {
            pattern.execute(vibrator)
        }
        if (waitForCompletion) {
            delay(pattern.duration)
        }
    }

    interface VibrationPattern {

        val duration: Duration

        fun execute(vibrator: Vibrator)
    }

    data class Params(
        val patternName: String,
        val waitForCompletion: Boolean,
    )

    companion object {

        internal fun findPattern(patternName: String): VibrationPattern =
            when (patternName.lowercase().filter { it.isLetterOrDigit() }) {
                "1", "long" -> object : VibrationPattern {
                    override val duration = 1.seconds

                    override fun execute(vibrator: Vibrator) {
                        vibrator.vibrate(VibrationEffect.createOneShot(duration.inWholeMilliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
                "2", "3pulses" -> object : VibrationPattern {
                    override val duration = 1000.milliseconds

                    override fun execute(vibrator: Vibrator) {
                        val pattern = longArrayOf(0L, 200L, 200L, 200L, 200L, 200L)
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    }
                }
                "click" -> object : VibrationPattern {
                    override val duration = 100.milliseconds

                    override fun execute(vibrator: Vibrator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        } else {
                            vibrator.vibrate(VibrationEffect.createOneShot(duration.inWholeMilliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    }
                }
                "tick" -> object : VibrationPattern {
                    override val duration = 100.milliseconds

                    override fun execute(vibrator: Vibrator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                        } else {
                            vibrator.vibrate(VibrationEffect.createOneShot(duration.inWholeMilliseconds, 100))
                        }
                    }
                }
                "heavyclick" -> object : VibrationPattern {
                    override val duration = 100.milliseconds

                    override fun execute(vibrator: Vibrator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                        } else {
                            vibrator.vibrate(VibrationEffect.createOneShot(duration.inWholeMilliseconds, 255))
                        }
                    }
                }
                "doubleclick" -> object : VibrationPattern {
                    override val duration = 300.milliseconds

                    override fun execute(vibrator: Vibrator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                        } else {
                            val pattern = longArrayOf(0L, 100L, 100L, 100L)
                            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                        }
                    }
                }
                "0", "3", "short", "" -> object : VibrationPattern {
                    override val duration = 300.milliseconds

                    override fun execute(vibrator: Vibrator) {
                        vibrator.vibrate(VibrationEffect.createOneShot(duration.inWholeMilliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
                else -> userError { "Unknown vibration pattern: $patternName" }
            }
    }
}
