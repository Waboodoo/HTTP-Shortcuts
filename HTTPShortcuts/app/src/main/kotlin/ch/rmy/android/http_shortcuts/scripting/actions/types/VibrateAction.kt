package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.VibrationUtil
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VibrateAction(private val patternId: Int, private val waitForCompletion: Boolean) : BaseAction() {

    @Inject
    lateinit var vibrationUtil: VibrationUtil

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun execute(executionContext: ExecutionContext): Completable {
        val vibrator = vibrationUtil.getVibrator()
            ?: return Completable.complete()

        val pattern = findPattern(patternId)
        return Completable.fromAction {
            pattern.execute(vibrator)
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .runIf(waitForCompletion) {
                delay(pattern.duration, TimeUnit.MILLISECONDS)
            }
    }

    interface VibrationPattern {

        val duration: Long

        fun execute(vibrator: Vibrator)
    }

    companion object {

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
