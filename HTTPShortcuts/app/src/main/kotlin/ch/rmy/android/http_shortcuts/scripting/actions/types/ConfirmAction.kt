package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

class ConfirmAction(private val message: String) : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<String> {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds()
        )

        return if (finalMessage.isNotEmpty()) {
            Single.create<String> { emitter ->
                DialogBuilder(executionContext.context)
                    .message(finalMessage)
                    .positive(R.string.dialog_ok) {
                        emitter.onSuccess(true.toString())
                    }
                    .negative(R.string.dialog_cancel) {
                        emitter.onSuccess(false.toString())
                    }
                    .dismissListener { emitter.onSuccess(false.toString()) }
                    .show()
            }
                .subscribeOn(AndroidSchedulers.mainThread())
        } else {
            Single.just(NO_RESULT)
        }
    }

}