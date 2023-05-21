package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import javax.inject.Inject

class SelectType : BaseVariableType() {

    @Inject
    lateinit var variablesRepository: VariableRepository

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun resolveValue(variable: Variable, dialogHandle: DialogHandle): String {
        val value = if (isMultiSelect(variable)) {
            dialogHandle.showDialog(
                ExecuteDialogState.MultiSelection(
                    title = variable.title.takeUnlessEmpty()?.toLocalizable(),
                    values = variable.options!!.map { option ->
                        option.id to option.labelOrValue
                    },
                )
            )
                .mapNotNull { optionId ->
                    variable.options!!.find { it.id == optionId }
                }
                .joinToString(getSeparator(variable)) { option ->
                    option.value
                }
        } else {
            dialogHandle.showDialog(
                ExecuteDialogState.Selection(
                    title = variable.title.takeUnlessEmpty()?.toLocalizable(),
                    values = variable.options!!.map { option ->
                        option.value to option.labelOrValue
                    },
                )
            )
        }

        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, value)
        }
        return value
    }

    companion object {
        const val KEY_MULTI_SELECT = "multi_select"
        const val KEY_SEPARATOR = "separator"

        fun isMultiSelect(variable: Variable) =
            variable.dataForType[KEY_MULTI_SELECT]?.toBoolean() ?: false

        fun getSeparator(variable: Variable) =
            variable.dataForType[KEY_SEPARATOR] ?: ","
    }
}
