package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.showSoftKeyboard
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.toDestroyable
import ch.rmy.android.framework.utils.Destroyable
import ch.rmy.android.http_shortcuts.R

object VariableViewUtils {

    fun bindVariableViews(
        editText: VariableEditText,
        button: VariableButton,
        variablePlaceholderProvider: VariablePlaceholderProvider,
        allowEditing: Boolean = true,
    ): Destroyable {
        editText.variablePlaceholderProvider = variablePlaceholderProvider
        button.variablePlaceholderProvider = variablePlaceholderProvider
        button.allowEditing = allowEditing
        return button.variableSource
            .subscribe(
                { variablePlaceholder ->
                    editText.insertVariablePlaceholder(variablePlaceholder)
                    editText.showSoftKeyboard()
                },
                { error ->
                    logException(error)
                    editText.context.showToast(R.string.error_generic)
                }
            )
            .toDestroyable()
    }
}
