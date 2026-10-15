package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class PromptNumberSliderActionType
@Inject
constructor(
    private val promptNumberSliderAction: PromptNumberSliderAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs): ActionRunnable<PromptNumberSliderAction.Params> {
        val options = args.getObject(0) ?: emptyMap()
        var value = options["value"]?.toString()?.toFloatOrNull()
        val min = options["min"]?.toString()?.toFloatOrNull() ?: (0f.coerceAtMost(value ?: 0f))
        val max = options["max"]?.toString()?.toFloatOrNull() ?: (((value ?: 50f) * 2).coerceAtLeast(100f))
        val stepSize = options["stepSize"]?.toString()?.toFloatOrNull() ?: 1f
        if (value == null) {
            value = min
        }

        if (stepSize <= 0) {
            throw ActionException {
                getString(
                    R.string.error_slider_error,
                    getString(R.string.error_slider_step_size_must_be_positive),
                )
            }
        }
        if (min > max) {
            throw ActionException {
                getString(
                    R.string.error_slider_error,
                    getString(R.string.error_slider_max_not_greater_than_min),
                )
            }
        }
        if (value !in min..max) {
            throw ActionException {
                getString(
                    R.string.error_slider_error,
                    getString(R.string.error_slider_value_must_be_between_min_and_max),
                )
            }
        }

        return ActionRunnable(
            action = promptNumberSliderAction,
            params = PromptNumberSliderAction.Params(
                message = options["text"]?.toString() ?: "",
                title = options["title"]?.toString(),
                initialValue = value,
                min = min,
                max = max,
                stepSize = stepSize,
                prefix = options["prefix"]?.toString() ?: "",
                suffix = options["suffix"]?.toString() ?: "",
            ),
        )
    }

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 1,
    )

    companion object {
        private const val TYPE = "prompt_slider_number"
        private const val FUNCTION_NAME = "promptNumberSlider"
    }
}
