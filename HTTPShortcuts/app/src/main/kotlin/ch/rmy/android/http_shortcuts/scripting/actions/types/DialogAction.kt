package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import javax.inject.Inject

class DialogAction(private val message: String, private val title: String) : BaseAction() {

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext) {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds(),
        )
        if (finalMessage.isEmpty()) {
            return
        }

        try {
            executionContext.dialogHandle.showDialog(
                ExecuteDialogState.RichTextDisplay(
                    title = title,
                    message = finalMessage,
                )
            )
        } catch (e: DialogCancellationException) {
            // proceed as normal
        }
    }
}
