package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableEditorActivity
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.isDarkThemeEnabled
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.setTintCompat
import ch.rmy.android.http_shortcuts.extensions.startActivity
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


open class VariableButton : AppCompatImageButton {

    lateinit var variablePlaceholderProvider: VariablePlaceholderProvider

    val variableSource: Observable<VariablePlaceholder>
        get() = variableSubject

    private val variableSubject = PublishSubject.create<VariablePlaceholder>()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setOnClickListener {
            if (hasVariables()) {
                openVariableSelectionDialog()
            } else {
                openInstructionDialog()
            }
        }

        if (context.isDarkThemeEnabled()) {
            drawable?.setTintCompat(Color.WHITE)
        }
    }

    protected open fun hasVariables() = variablePlaceholderProvider.hasVariables

    private fun openInstructionDialog() {
        DialogBuilder(context)
            .title(R.string.help_title_variables)
            .message(if (isUsedFromVariableEditor()) R.string.help_text_variable_button_for_variables else R.string.help_text_variable_button)
            .positive(android.R.string.ok)
            .mapIf(!isUsedFromVariableEditor()) {
                it.neutral(R.string.button_create_first_variable) { openVariableEditor() }
            }
            .show()
    }

    private fun openVariableEditor() {
        VariablesActivity.IntentBuilder(context)
            .build()
            .startActivity(context)
    }

    private fun openVariableSelectionDialog() {
        DialogBuilder(context)
            .title(getTitle())
            .mapFor(getVariables()) { builder, placeholder ->
                builder.item(placeholder.variableKey) {
                    variableSubject.onNext(placeholder)
                }
            }
            .mapIf(!isUsedFromVariableEditor()) {
                it.neutral(R.string.label_edit_variables) { openVariableEditor() }
            }
            .showIfPossible()
    }

    protected open fun getTitle() = R.string.dialog_title_variable_selection

    protected open fun getVariables() = variablePlaceholderProvider.placeholders

    private fun isUsedFromVariableEditor() = context is VariableEditorActivity

}