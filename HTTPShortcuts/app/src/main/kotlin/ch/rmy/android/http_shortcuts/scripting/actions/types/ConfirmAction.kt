package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class ConfirmAction(private val message: String) : BaseAction() {

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext): Boolean? {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds(),
        )

        if (finalMessage.isEmpty()) {
            return null
        }

        return try {
            executionContext.dialogHandle.showDialog(
                ExecuteDialogState.GenericConfirm(finalMessage.toLocalizable())
            )
            true
        } catch (e: CancellationException) {
            false
        }
    }
}
