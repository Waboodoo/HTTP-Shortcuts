package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import android.util.AttributeSet
import android.widget.AutoCompleteTextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.Destroyable
import ch.rmy.android.http_shortcuts.utils.color
import ch.rmy.android.http_shortcuts.utils.showSoftKeyboard

class VariableEditText : AutoCompleteTextView {

    private val variableColor by lazy {
        color(context, R.color.variable)
    }

    private lateinit var variables: List<Variable>

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var rawString: String
        get() = Variables.variableSpansToRawPlaceholders(text)
        set(value) {
            val processedText = Variables.rawPlaceholdersToVariableSpans(value, variables, variableColor)
            setText(processedText)
            setSelection(processedText.length)
        }

    fun insertVariablePlaceholder(variableKey: String) {
        val position = selectionEnd.takeIf { it != -1 } ?: text.length
        Variables.insertVariableSpan(text, variableKey, position, variableColor)
    }

    fun bind(variableButton: VariableButton, variables: List<Variable>): Destroyable {
        this.variables = variables
        variableButton.variables = variables
        return variableButton.variableSource.add {variable ->
            insertVariablePlaceholder(variable.key)
            showSoftKeyboard()
        }
    }

}