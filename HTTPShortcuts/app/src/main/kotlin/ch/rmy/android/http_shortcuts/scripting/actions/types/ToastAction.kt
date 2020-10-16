package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

class ToastAction(private val message: String) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds(),
        )
        return if (finalMessage.isNotEmpty()) {
            Completable
                .fromAction {
                    executionContext.context.showToast(finalMessage, long = true)
                }
                .subscribeOn(AndroidSchedulers.mainThread())
        } else {
            Completable.complete()
        }
    }

}