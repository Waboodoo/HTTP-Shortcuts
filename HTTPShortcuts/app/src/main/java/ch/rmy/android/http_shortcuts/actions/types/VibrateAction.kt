package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.utils.PromiseUtils
import org.jdeferred2.Promise

class VibrateAction(actionDTO: ActionDTO) : BaseAction(actionDTO) {

    override fun getTitle(context: Context) = context.getString(R.string.action_type_vibrate_title)

    override fun getDescription(context: Context) = pattern.getDescription(context)

    override fun perform(context: Context, shortcutId: Long, variableValues: Map<String, String>): Promise<Unit, Exception, Unit> {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (!vibrator.hasVibrator()) {
            return PromiseUtils.resolve(Unit)
        }
        val pattern = pattern
        pattern.execute(vibrator)
        return if (waitForCompletion) {
            PromiseUtils.resolveDelayed(Unit, pattern.duration)
        } else {
            PromiseUtils.resolve(Unit)
        }
    }

    private val patternId
        get() = (action.data[KEY_PATTERN]?.toIntOrNull()) ?: 0

    private val waitForCompletion
        get() = (action.data[KEY_WAIT_FOR_COMPLETION]?.toBoolean()) ?: false

    private val pattern: VibrationPattern
        get() = findPattern(patternId)

    private fun findPattern(patternId: Int): VibrationPattern {
        return when (patternId) {
            1 -> object : VibrationPattern {
                override val duration: Long
                    get() = 1000L

                override fun execute(vibrator: Vibrator) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        //deprecated in API 26
                        vibrator.vibrate(duration)
                    }
                }

                override fun getDescription(context: Context): CharSequence =
                        context.getString(R.string.action_type_vibrate_description_pattern_1)
            }
            2 -> object : VibrationPattern {
                override val duration: Long
                    get() = 1200L

                override fun execute(vibrator: Vibrator) {
                    val pattern = longArrayOf(200L, 200L, 200L, 200L, 200L, 200L)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } else {
                        //deprecated in API 26
                        vibrator.vibrate(pattern, -1)
                    }
                }

                override fun getDescription(context: Context): CharSequence =
                        context.getString(R.string.action_type_vibrate_description_pattern_2)
            }
            else -> object : VibrationPattern {
                override val duration: Long
                    get() = 300L

                override fun execute(vibrator: Vibrator) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        //deprecated in API 26
                        vibrator.vibrate(duration)
                    }
                }

                override fun getDescription(context: Context): CharSequence =
                        context.getString(R.string.action_type_vibrate_description_pattern_0)
            }
        }
    }

    interface VibrationPattern {

        val duration: Long

        fun execute(vibrator: Vibrator)

        fun getDescription(context: Context): CharSequence

    }

    companion object {

        const val TYPE = "vibrate"

        const val KEY_PATTERN = "pattern"
        const val KEY_WAIT_FOR_COMPLETION = "wait"

    }

}