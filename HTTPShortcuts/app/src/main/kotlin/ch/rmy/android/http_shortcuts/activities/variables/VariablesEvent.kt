package ch.rmy.android.http_shortcuts.activities.variables

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.enums.VariableType

abstract class VariablesEvent : ViewModelEvent() {
    data class ShowCreationDialog(
        val variableOptions: List<VariableTypeOption>,
    ) : VariablesEvent() {
        sealed interface VariableTypeOption {
            data class Variable(val type: VariableType, val name: Localizable) : VariableTypeOption
            object Separator : VariableTypeOption
        }
    }

    data class ShowContextMenu(
        val variableId: String,
        val title: Localizable,
    ) : VariablesEvent()

    data class ShowDeletionDialog(
        val variableId: String,
        val title: String,
        val message: Localizable,
    ) : VariablesEvent()
}
