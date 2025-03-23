package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import javax.inject.Inject

class SelectType
@Inject
constructor(
    private val variablesRepository: VariableRepository,
) : VariableType {
    override suspend fun resolve(variable: Variable, dialogHandle: DialogHandle): String {
        val value = if (isMultiSelect(variable)) {
            dialogHandle.showDialog(
                ExecuteDialogState.MultiSelection(
                    title = variable.title.takeUnlessEmpty()?.toLocalizable(),
                    values = run {
                        val labels = variable.getStringListData(KEY_LABELS) ?: emptyList()
                        val values = variable.getStringListData(KEY_VALUES) ?: emptyList()
                        values.mapIndexed { index, value ->
                            value to (labels[index].ifEmpty { value }.ifEmpty { "-" })
                        }
                    },
                ),
            )
                .joinToString(getSeparator(variable)) { value ->
                    value
                }
        } else {
            dialogHandle.showDialog(
                ExecuteDialogState.Selection(
                    title = variable.title.takeUnlessEmpty()?.toLocalizable(),
                    values = run {
                        val labels = variable.getStringListData(KEY_LABELS) ?: emptyList()
                        val values = variable.getStringListData(KEY_VALUES) ?: emptyList()
                        values.mapIndexed { index, value ->
                            value to (labels[index].ifEmpty { value }.ifEmpty { "-" })
                        }
                    },
                ),
            )
        }

        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, value)
        }
        return value
    }

    companion object {
        const val KEY_LABELS = "labels"
        const val KEY_VALUES = "values"
        const val KEY_MULTI_SELECT = "multi_select"
        const val KEY_SEPARATOR = "separator"

        fun isMultiSelect(variable: Variable) =
            variable.getBooleanData(KEY_MULTI_SELECT) == true

        fun getSeparator(variable: Variable) =
            variable.getStringData(KEY_SEPARATOR) ?: ","
    }
}
