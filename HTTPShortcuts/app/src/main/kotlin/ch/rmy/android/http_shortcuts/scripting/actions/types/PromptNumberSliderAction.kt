package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject
import kotlin.Float

class PromptNumberSliderAction
@Inject
constructor() : Action<PromptNumberSliderAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): Double? =
        try {
            if (max <= min) {
                throw ActionException {
                    getString(R.string.error_slider_max_not_greater_than_min)
                }
            }
            if (stepSize <= 0) {
                throw ActionException {
                    getString(R.string.error_slider_step_size_must_be_positive)
                }
            }
            executionContext.dialogHandle.showDialog(
                ExecuteDialogState.NumberSlider(
                    message = message.toLocalizable(),
                    title = title?.toLocalizable(),
                    initialValue = initialValue,
                    min = min,
                    max = max,
                    stepSize = stepSize,
                    prefix = prefix,
                    suffix = suffix,
                ),
            )
                .toDouble()
        } catch (_: DialogCancellationException) {
            null
        }

    data class Params(
        val message: String,
        val title: String?,
        val initialValue: Float?,
        val min: Float,
        val max: Float,
        val stepSize: Float,
        val prefix: String,
        val suffix: String,
    )
}
