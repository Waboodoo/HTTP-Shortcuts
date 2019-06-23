package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import com.android.volley.VolleyError
import io.reactivex.Completable
import java.util.concurrent.TimeUnit

class VibrateAction(actionType: VibrateActionType, data: Map<String, String>) : BaseAction(actionType, data) {

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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

    var patternId
        get() = (internalData[KEY_PATTERN]?.toIntOrNull()) ?: 0
        set(value) {
            internalData[KEY_PATTERN] = value.toString()
        }

    var waitForCompletion
        get() = (internalData[KEY_WAIT_FOR_COMPLETION]?.toBoolean()) ?: false
        set(value) {
            internalData[KEY_WAIT_FOR_COMPLETION] = value.toString()
        }

    private val pattern: VibrationPattern
        get() = findPattern(patternId)

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) = VibrateActionEditorView(context, this)

    interface VibrationPattern {

        val duration: Long

        fun execute(vibrator: Vibrator)

        fun getDescription(context: Context): String

    }

    companion object {

        const val KEY_PATTERN = "pattern"
        const val KEY_WAIT_FOR_COMPLETION = "wait"

        private const val PATTERN_COUNT = 3

        fun getPatterns() = (0 until PATTERN_COUNT).map { findPattern(it) }

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

                    override fun getDescription(context: Context): String =
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
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(pattern, -1)
                        }
                    }

                    override fun getDescription(context: Context): String =
                        context.getString(R.string.action_type_vibrate_description_pattern_2)
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

                    override fun getDescription(context: Context): String =
                        context.getString(R.string.action_type_vibrate_description_pattern_0)
                }
            }

    }

}