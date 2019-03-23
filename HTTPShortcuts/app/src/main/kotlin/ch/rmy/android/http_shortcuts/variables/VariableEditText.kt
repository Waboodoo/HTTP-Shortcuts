package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.showSoftKeyboard
import ch.rmy.android.http_shortcuts.extensions.toDestroyable
import ch.rmy.android.http_shortcuts.utils.Destroyable

class VariableEditText : AppCompatAutoCompleteTextView {

    private lateinit var variablePlaceholderProvider: VariablePlaceholderProvider

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var rawString: String
        get() = Variables.variableSpansToRawPlaceholders(text)
        set(value) {
            val processedText = Variables.rawPlaceholdersToVariableSpans(value, variablePlaceholderProvider)
            setText(processedText)
            try {
                setSelection(text.length)
            } catch (e: Exception) {
                logException(e)
            }
        }

    private fun insertVariablePlaceholder(placeholder: VariablePlaceholder) {
        val position = selectionEnd.takeIf { it != -1 } ?: text.length
        Variables.insertVariableSpan(text, placeholder, position)
    }

    fun bind(variableButton: VariableButton, variablePlaceholderProvider: VariablePlaceholderProvider): Destroyable {
        this.variablePlaceholderProvider = variablePlaceholderProvider
        variableButton.variablePlaceholderProvider = variablePlaceholderProvider
        return variableButton.variableSource.subscribe { placeholder ->
            insertVariablePlaceholder(placeholder)
            showSoftKeyboard()
        }.toDestroyable()
    }

}