package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.extensions.showOrElse
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class PromptAction(private val message: String, private val prefill: String) : BaseAction() {

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

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
                DialogBuilder(activityProvider.getActivity())
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
                        continuation.cancel()
                    }
            }
        }
            .takeUnlessEmpty()
            ?.removePrefix("-")
    }
}
