package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import ch.rmy.android.framework.extensions.isDarkThemeEnabled
import ch.rmy.android.framework.extensions.mapFor
import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.framework.extensions.setTintCompat
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class VariableButton : AppCompatImageButton {

    lateinit var variablePlaceholderProvider: VariablePlaceholderProvider
    var allowEditing = true

    val variableSource: Observable<VariablePlaceholder>
        get() = variableSubject

    private val variableSubject = PublishSubject.create<VariablePlaceholder>()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setImageResource(R.drawable.ic_variables)
        setOnClickListener {
            if (variablePlaceholderProvider.hasVariables) {
                openVariableSelectionDialog()
            } else {
                openInstructionDialog()
            }
        }

        if (context.isDarkThemeEnabled()) {
            drawable?.setTintCompat(Color.WHITE)
        }
    }

    private fun openInstructionDialog() {
        DialogBuilder(context)
            .title(R.string.help_title_variables)
            .message(if (allowEditing) R.string.help_text_variable_button else R.string.help_text_variable_button_for_variables)
            .positive(android.R.string.ok)
            .mapIf(allowEditing) {
                neutral(R.string.button_create_first_variable) { openVariableEditor() }
            }
            .show()
    }

    private fun openVariableEditor() {
        VariablesActivity.IntentBuilder()
            .startActivity(context)
    }

    private fun openVariableSelectionDialog() {
        DialogBuilder(context)
            .title(R.string.dialog_title_variable_selection)
            .mapFor(variablePlaceholderProvider.placeholders) { placeholder ->
                item(name = placeholder.variableKey) {
                    variableSubject.onNext(placeholder)
                }
            }
            .mapIf(allowEditing) {
                neutral(R.string.label_edit_variables) { openVariableEditor() }
            }
            .showIfPossible()
    }
}
