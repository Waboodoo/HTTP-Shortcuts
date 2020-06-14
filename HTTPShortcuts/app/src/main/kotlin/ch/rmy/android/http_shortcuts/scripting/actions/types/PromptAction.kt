package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

class PromptAction(data: Map<String, String>) : BaseAction() {

    private val message: String = data[KEY_MESSAGE] ?: ""

    private val prefill: String = data[KEY_PREFILL] ?: ""

    override fun executeForValue(executionContext: ExecutionContext): Single<String> {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds()
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
                    it.takeUnless { it.isEmpty() }
                        ?.removePrefix("-")
                        ?: NO_RESULT
                }
        } else {
            Single.just(NO_RESULT)
        }
    }

    companion object {

        const val KEY_MESSAGE = "message"
        const val KEY_PREFILL = "prefill"

    }

}