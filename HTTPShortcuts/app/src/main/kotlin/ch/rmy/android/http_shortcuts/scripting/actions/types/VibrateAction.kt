package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.os.VibrationEffect
import android.os.Vibrator
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

        val pattern = findPattern(patternId)
        withContext(Dispatchers.Main) {
            pattern.execute(vibrator)
        }
        if (waitForCompletion) {
            delay(pattern.duration)
        }
    }

    interface VibrationPattern {

        val duration: Duration

        val milliseconds: Long
            get() = duration.inWholeMilliseconds

        fun execute(vibrator: Vibrator)
    }

    data class Params(
        val patternId: Int,
        val waitForCompletion: Boolean,
    )

    companion object {

        internal fun findPattern(patternId: Int): VibrationPattern =
            when (patternId) {
                1 -> object : VibrationPattern {
                    override val duration = 1.seconds

                    override fun execute(vibrator: Vibrator) {
                        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
                2 -> object : VibrationPattern {
                    override val duration = 1200.milliseconds

                    override fun execute(vibrator: Vibrator) {
                        val pattern = longArrayOf(200L, 200L, 200L, 200L, 200L, 200L)
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    }
                }
                else -> object : VibrationPattern {
                    override val duration = 300.milliseconds

                    override fun execute(vibrator: Vibrator) {
                        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
            }
    }
}
