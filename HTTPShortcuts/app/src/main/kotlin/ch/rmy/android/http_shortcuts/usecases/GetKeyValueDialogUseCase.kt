package ch.rmy.android.http_shortcuts.usecases

import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.TextView
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.setText
import ch.rmy.android.framework.extensions.showSoftKeyboard
import ch.rmy.android.framework.utils.Destroyer
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import javax.inject.Inject

class GetKeyValueDialogUseCase
@Inject
constructor(
    private val variableViewUtils: VariableViewUtils,
) {

    operator fun invoke(
        title: Localizable,
        keyLabel: Localizable,
        valueLabel: Localizable,
        key: String? = null,
        value: String? = null,
        isMultiLine: Boolean = false,
        suggestions: Array<String>? = null,
        keyValidator: (CharSequence) -> String? = { _ -> null },
        valueValidator: (CharSequence) -> String? = { _ -> null },
        onConfirm: (key: String, value: String) -> Unit,
        onRemove: () -> Unit,
    ): DialogState {
        val destroyer = Destroyer()
        return DialogState.create(id = "key-value-dialog", onDismiss = destroyer::destroy) {
            view(R.layout.dialog_key_value_editor)
                .title(title)
                .canceledOnTouchOutside(false)
                .positive(R.string.dialog_ok) { dialog ->
                    val keyField = dialog.findViewById<VariableEditText>(R.id.key_value_key)
                    val valueField = dialog.findViewById<VariableEditText>(R.id.key_value_value)

                    val keyText = keyField.rawString
                    val valueText = valueField.rawString
                    onConfirm(keyText, valueText)
                }
                .runIf(key != null) {
                    neutral(R.string.dialog_remove) {
                        onRemove()
                    }
                }
                .negative(R.string.dialog_cancel)
                .dismissListener {
                    destroyer.destroy()
                }
                .build()
                .also { dialog ->
                    val keyInput = dialog.findViewById<VariableEditText>(R.id.key_value_key)
                    val valueInput = dialog.findViewById<VariableEditText>(R.id.key_value_value)
                    val keyVariableButton = dialog.findViewById(R.id.variable_button_key) as VariableButton
                    val valueVariableButton = dialog.findViewById(R.id.variable_button_value) as VariableButton

                    variableViewUtils.bindVariableViews(keyInput, keyVariableButton)
                    variableViewUtils.bindVariableViews(valueInput, valueVariableButton)

                    valueInput.inputType = (if (isMultiLine) InputType.TYPE_TEXT_FLAG_MULTI_LINE else 0) or InputType.TYPE_CLASS_TEXT
                    if (isMultiLine) {
                        valueInput.maxLines = MAX_LINES
                    }

                    (dialog.findViewById(R.id.label_key_value) as TextView).setText(keyLabel)
                    (dialog.findViewById(R.id.label_value_value) as TextView).setText(valueLabel)

                    if (key != null) {
                        keyInput.rawString = key
                    }
                    if (value != null) {
                        valueInput.rawString = value
                    }

                    if (suggestions != null) {
                        keyInput.setAdapter(ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, suggestions))
                    }

                    dialog.setOnShowListener {
                        keyInput.showSoftKeyboard()
                    }

                    val okButton = dialog.getActionButton(WhichButton.POSITIVE)
                    okButton.isEnabled = keyInput.text.isNotEmpty()
                    keyInput.observeTextChanges()
                        .subscribe { text ->
                            keyInput.error = keyValidator.invoke(text)
                            okButton.isEnabled = keyInput.text.isNotEmpty() && keyInput.error == null && valueInput.error == null
                        }
                        .attachTo(destroyer)
                    valueInput.observeTextChanges()
                        .subscribe { text ->
                            valueInput.error = valueValidator.invoke(text)
                            okButton.isEnabled = keyInput.text.isNotEmpty() && keyInput.error == null && valueInput.error == null
                        }
                        .attachTo(destroyer)
                }
        }
    }

    companion object {

        private const val MAX_LINES = 5
    }
}
