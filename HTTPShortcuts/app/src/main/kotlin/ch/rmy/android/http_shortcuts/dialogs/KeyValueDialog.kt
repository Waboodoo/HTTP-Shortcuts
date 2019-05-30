package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import android.text.InputType
import android.widget.ArrayAdapter
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import ch.rmy.android.http_shortcuts.extensions.showSoftKeyboard
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.Maybe
import io.reactivex.subjects.MaybeSubject

class KeyValueDialog(
    private val variablePlaceholderProvider: VariablePlaceholderProvider,
    private val title: CharSequence,
    private val data: Pair<String, String>? = null,
    private val keyLabel: CharSequence? = null,
    private val valueLabel: CharSequence? = null,
    private val isMultiLine: Boolean = false,
    private val suggestions: Array<String>? = null
) {

    fun show(context: Context): Maybe<Event> {
        val subject = MaybeSubject.create<Event>()

        val destroyer = Destroyer()
        MaterialDialog.Builder(context)
            .customView(R.layout.dialog_key_value_editor, false)
            .title(title)
            .positiveText(R.string.dialog_ok)
            .canceledOnTouchOutside(false)
            .onPositive { dialog, _ ->
                val keyField = dialog.findViewById(R.id.key_value_key) as VariableEditText
                val valueField = dialog.findViewById(R.id.key_value_value) as VariableEditText

                val keyText = keyField.rawString
                val valueText = valueField.rawString
                subject.onSuccess(DataChangedEvent(keyText to valueText))
            }
            .mapIf(data != null) {
                it.neutralText(R.string.dialog_remove)
                    .onNeutral { _, _ ->
                        subject.onSuccess(DataRemovedEvent())
                    }
            }
            .negativeText(R.string.dialog_cancel)
            .dismissListener {
                subject.onComplete()
                destroyer.destroy()
            }
            .build()
            .also { dialog ->
                val keyInput = dialog.findViewById(R.id.key_value_key) as VariableEditText
                val valueInput = dialog.findViewById(R.id.key_value_value) as VariableEditText
                val keyVariableButton = dialog.findViewById(R.id.variable_button_key) as VariableButton
                val valueVariableButton = dialog.findViewById(R.id.variable_button_value) as VariableButton

                VariableViewUtils.bindVariableViews(keyInput, keyVariableButton, variablePlaceholderProvider)
                    .attachTo(destroyer)
                VariableViewUtils.bindVariableViews(valueInput, valueVariableButton, variablePlaceholderProvider)
                    .attachTo(destroyer)

                valueInput.inputType = (if (isMultiLine) InputType.TYPE_TEXT_FLAG_MULTI_LINE else 0) or InputType.TYPE_CLASS_TEXT
                if (isMultiLine) {
                    valueInput.maxLines = MAX_LINES
                }

                (dialog.findViewById(R.id.key_value_key_layout) as TextInputLayout).hint = keyLabel
                (dialog.findViewById(R.id.key_value_value_layout) as TextInputLayout).hint = valueLabel

                if (data != null) {
                    keyInput.rawString = data.first
                    valueInput.rawString = data.second
                }

                if (suggestions != null) {
                    keyInput.setAdapter<ArrayAdapter<String>>(ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, suggestions))
                }

                dialog.setOnShowListener {
                    keyInput.showSoftKeyboard()
                }

                val okButton = dialog.getActionButton(DialogAction.POSITIVE)
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

    abstract class Event

    class DataChangedEvent(val data: Pair<String, String>) : Event()

    class DataRemovedEvent : Event()

    companion object {

        private const val MAX_LINES = 5

    }

}