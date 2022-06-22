package ch.rmy.android.http_shortcuts.usecases

import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import javax.inject.Inject

class GetVariablePlaceholderPickerDialogUseCase
@Inject
constructor(
    private val variablePlaceholderProvider: VariablePlaceholderProvider,
) {
    operator fun invoke(
        onVariableSelected: (VariablePlaceholder) -> Unit,
        onEditVariableButtonClicked: (() -> Unit)? = null,
    ): DialogState =
        if (variablePlaceholderProvider.hasVariables) {
            createVariableSelectionDialog(
                variablePlaceholderProvider,
                onVariableSelected,
                onEditVariableButtonClicked,
            )
        } else {
            createInstructionDialog(onEditVariableButtonClicked)
        }

    private fun createVariableSelectionDialog(
        variablePlaceholderProvider: VariablePlaceholderProvider,
        onVariableSelected: (VariablePlaceholder) -> Unit,
        onEditVariableButtonClicked: (() -> Unit)?,
    ) =
        DialogState.create(id = "insert-variable-placeholder") {
            title(R.string.dialog_title_variable_selection)
                .runFor(variablePlaceholderProvider.placeholders) { placeholder ->
                    item(name = placeholder.variableKey, descriptionRes = VariableTypeMappings.getTypeName(placeholder.variableType)) {
                        onVariableSelected(placeholder)
                    }
                }
                .runIfNotNull(onEditVariableButtonClicked) { onClicked ->
                    neutral(R.string.label_edit_variables) { onClicked() }
                }
                .build()
        }

    private fun createInstructionDialog(onEditVariableButtonClicked: (() -> Unit)?) =
        DialogState.create(id = "variable-placeholder-instructions") {
            title(R.string.help_title_variables)
                .message(
                    if (onEditVariableButtonClicked != null) {
                        R.string.help_text_variable_button
                    } else {
                        R.string.help_text_variable_button_for_variables
                    }
                )
                .positive(android.R.string.ok)
                .runIfNotNull(onEditVariableButtonClicked) { onClicked ->
                    neutral(R.string.button_create_first_variable) { onClicked() }
                }
                .build()
        }
}
