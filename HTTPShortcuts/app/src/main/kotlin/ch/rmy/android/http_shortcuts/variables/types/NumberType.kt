package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import javax.inject.Inject

class NumberType : BaseVariableType() {

    @Inject
    lateinit var variablesRepository: VariableRepository

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun resolveValue(variable: Variable, dialogHandle: DialogHandle): String {
        val value = dialogHandle.showDialog(
            ExecuteDialogState.TextInput(
                title = variable.title.takeUnlessEmpty()?.toLocalizable(),
                message = variable.message.takeUnlessEmpty()?.toLocalizable(),
                initialValue = variable.value?.takeIf { variable.rememberValue } ?: "",
                type = ExecuteDialogState.TextInput.Type.NUMBER,
            ),
        )
            .let(::sanitize)
        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, value)
        }
        return value
    }

    private fun sanitize(input: String) =
        input.trimEnd('.')
            .let {
                when {
                    it.startsWith("-.") -> "-0.${it.drop(2)}"
                    it.startsWith(".") -> "0$it"
                    it.isEmpty() || it == "-" -> "0"
                    else -> it
                }
            }
}
