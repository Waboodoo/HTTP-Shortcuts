package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.VibrationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class VibrateAction(private val patternId: Int, private val waitForCompletion: Boolean) : BaseAction() {

    @Inject
    lateinit var vibrationUtil: VibrationUtil

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext) {
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

    companion object {

        private fun findPattern(patternId: Int): VibrationPattern =
            when (patternId) {
                1 -> object : VibrationPattern {
                    override val duration = 1.seconds

                    override fun execute(vibrator: Vibrator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(milliseconds)
                        }
                    }
                }
                2 -> object : VibrationPattern {
                    override val duration = 1200.milliseconds

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
                    override val duration = 300.milliseconds

                    override fun execute(vibrator: Vibrator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(milliseconds)
                        }
                    }
                }
            }
    }
}
