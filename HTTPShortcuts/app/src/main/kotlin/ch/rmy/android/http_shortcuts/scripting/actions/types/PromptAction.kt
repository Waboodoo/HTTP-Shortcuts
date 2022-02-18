package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

class PromptAction(private val message: String, private val prefill: String) : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<Any> {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds(),
        )

        return if (finalMessage.isNotEmpty()) {
            Single.create<String> { emitter ->
                DialogBuilder(executionContext.context)
                    .message(finalMessage)
                    .textInput(prefill = prefill) { input ->
                        emitter.onSuccess("-$input")
                    }
                    .dismissListener { emitter.onSuccess("") }
                    .show()
            }
                .subscribeOn(AndroidSchedulers.mainThread())
                .map {
                    it.takeUnlessEmpty()
                        ?.removePrefix("-")
                        ?: NO_RESULT
                }
        } else {
            Single.just(NO_RESULT)
        }
    }
}
