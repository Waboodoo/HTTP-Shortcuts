package ch.rmy.android.http_shortcuts.activities.editor.body.usecases

import android.view.View
import android.widget.EditText
import androidx.annotation.CheckResult
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.runIf
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

class GetFileParameterDialogUseCase
@Inject
constructor(
    private val variableViewUtils: VariableViewUtils,
) {

    @CheckResult
    operator fun invoke(
        title: Localizable,
        showRemoveOption: Boolean = false,
        showFileNameOption: Boolean = false,
        keyName: String = "",
        fileName: String = "",
        onConfirm: (keyName: String, fileName: String) -> Unit,
        onRemove: () -> Unit,
    ): DialogState {
        val destroyer = Destroyer()
        return DialogState.create(id = "get-file-parameter", onDismiss = destroyer::destroy) {
            view(R.layout.dialog_file_parameter_editor)
                .title(title)
                .canceledOnTouchOutside(false)
                .positive(R.string.dialog_ok) { dialog ->
                    val keyField = dialog.findViewById<VariableEditText>(R.id.key_value_key)
                    val fileNameField = dialog.findViewById<EditText>(R.id.key_file_name)

                    val keyText = keyField.rawString
                    onConfirm(keyText, fileNameField.text.toString())
                }
                .runIf(showRemoveOption) {
                    neutral(R.string.dialog_remove) {
                        onRemove()
                    }
                }
                .negative(R.string.dialog_cancel)
                .build()
                .also { dialog ->
                    val keyInput = dialog.findViewById<VariableEditText>(R.id.key_value_key)
                    val fileNameInput = dialog.findViewById<EditText>(R.id.key_file_name)
                    val keyVariableButton = dialog.findViewById(R.id.variable_button_key) as VariableButton

                    variableViewUtils.bindVariableViews(keyInput, keyVariableButton)

                    keyInput.rawString = keyName
                    fileNameInput.setText(fileName)

                    dialog.findViewById<View>(R.id.file_name_input_container).isVisible = showFileNameOption

                    dialog.setOnShowListener {
                        keyInput.showSoftKeyboard()
                    }

                    val okButton = dialog.getActionButton(WhichButton.POSITIVE)
                    okButton.isEnabled = keyInput.text.isNotEmpty()
                    keyInput.observeTextChanges()
                        .subscribe { text ->
                            okButton.isEnabled = text.isNotEmpty()
                        }
                        .attachTo(destroyer)
                }
        }
    }
}
