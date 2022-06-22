package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.framework.extensions.showIfPossible
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.usecases.GetVariablePlaceholderPickerDialogUseCase
import javax.inject.Inject

// TODO: Refactor this so that the dialog state that is created within is properly stored in the view state
class VariableViewUtils
@Inject
constructor(
    private val getVariablePlaceholderPickerDialog: GetVariablePlaceholderPickerDialogUseCase,
) {

    fun bindVariableViews(
        editText: VariableEditText,
        button: VariableButton,
        allowEditing: Boolean = true,
    ) {
        button.setOnClickListener {
            getVariablePlaceholderPickerDialog.invoke(
                onVariableSelected = {
                    editText.insertVariablePlaceholder(it)
                },
                onEditVariableButtonClicked = if (allowEditing) {
                    {
                        VariablesActivity.IntentBuilder()
                            .startActivity(editText.context)
                    }
                } else null,
            )
                .createDialog(editText.context)
                .showIfPossible()
        }
    }
}
