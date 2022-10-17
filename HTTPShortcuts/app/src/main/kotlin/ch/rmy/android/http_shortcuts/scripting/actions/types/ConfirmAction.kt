package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.canceledByUser
import ch.rmy.android.http_shortcuts.extensions.showOrElse
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class ConfirmAction(private val message: String) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext): Boolean? {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds(),
        )

        if (finalMessage.isEmpty()) {
            return null
        }

        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<Boolean> { continuation ->
                DialogBuilder(executionContext.context)
                    .message(finalMessage)
                    .positive(R.string.dialog_ok) {
                        continuation.resume(true)
                    }
                    .negative(R.string.dialog_cancel) {
                        continuation.resume(false)
                    }
                    .dismissListener {
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }
                    .showOrElse {
                        continuation.canceledByUser()
                    }
            }
        }
    }
}
