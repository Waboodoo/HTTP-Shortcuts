package ch.rmy.android.http_shortcuts.activities.editor.body

import android.content.Context
import android.view.View
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import ch.rmy.android.http_shortcuts.extensions.showSoftKeyboard
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import io.reactivex.Maybe
import io.reactivex.subjects.MaybeSubject

class FileParameterDialog(
    private val variablePlaceholderProvider: VariablePlaceholderProvider,
    private val title: String,
    private val showRemoveOption: Boolean = false,
    private val showFileNameOption: Boolean = false,
    private val keyName: String = "",
    private val fileName: String = ""
) {

    fun show(context: Context): Maybe<Event> {
        val subject = MaybeSubject.create<Event>()

        val destroyer = Destroyer()
        DialogBuilder(context)
            .view(R.layout.dialog_file_parameter_editor)
            .title(title)
            .canceledOnTouchOutside(false)
            .positive(R.string.dialog_ok) { dialog ->
                val keyField = dialog.findViewById(R.id.key_value_key) as VariableEditText
                val fileNameField = dialog.findViewById(R.id.key_file_name) as EditText

                val keyText = keyField.rawString
                subject.onSuccess(Event.DataChangedEvent(keyName = keyText, fileName = fileNameField.text.toString()))
            }
            .mapIf(showRemoveOption) {
                it.neutral(R.string.dialog_remove) {
                    subject.onSuccess(Event.DataRemovedEvent)
                }
            }
            .negative(R.string.dialog_cancel)
            .dismissListener {
                subject.onComplete()
                destroyer.destroy()
            }
            .build()
            .also { dialog ->
                val keyInput = dialog.findViewById(R.id.key_value_key) as VariableEditText
                val fileNameInput = dialog.findViewById(R.id.key_file_name) as EditText
                val keyVariableButton = dialog.findViewById(R.id.variable_button_key) as VariableButton

                VariableViewUtils.bindVariableViews(keyInput, keyVariableButton, variablePlaceholderProvider)
                    .attachTo(destroyer)

                keyInput.rawString = keyName
                fileNameInput.setText(fileName)

                dialog.findViewById<View>(R.id.file_name_input_container).visible = showFileNameOption

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
            .showIfPossible()
            ?: run {
                subject.onComplete()
            }
        return subject
    }

    sealed class Event {

        class DataChangedEvent(val keyName: String, val fileName: String) : Event()

        object DataRemovedEvent : Event()

    }

}