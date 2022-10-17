package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.extensions.canceledByUser
import ch.rmy.android.http_shortcuts.extensions.showOrElse
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class PromptAction(private val message: String, private val prefill: String) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext): String? {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds(),
        )

        if (finalMessage.isEmpty()) {
            return null
        }

        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<String> { continuation ->
                DialogBuilder(executionContext.context)
                    .message(finalMessage)
                    .textInput(prefill = prefill) { input ->
                        continuation.resume("-$input")
                    }
                    .dismissListener {
                        if (continuation.isActive) {
                            continuation.resume("")
                        }
                    }
                    .showOrElse {
                        continuation.canceledByUser()
                    }
            }
        }
            .takeUnlessEmpty()
            ?.removePrefix("-")
    }
}
