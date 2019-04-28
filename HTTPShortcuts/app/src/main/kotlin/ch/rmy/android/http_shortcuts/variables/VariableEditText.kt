package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import android.text.Spannable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.setTextSafely

class VariableEditText : AppCompatAutoCompleteTextView {

    var variablePlaceholderProvider: VariablePlaceholderProvider? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val placeholderColor by lazy {
        color(context, R.color.variable)
    }

    var rawString: String
        get() = Variables.variableSpansToRawPlaceholders(text)
        set(value) {
            val newText = variablePlaceholderProvider
                ?.let { variablePlaceholderProvider ->
                    Variables.rawPlaceholdersToVariableSpans(value, variablePlaceholderProvider, placeholderColor)
                } ?: value

            if ((text ?: "").toString() != newText.toString()) {
                if (text.isEmpty()) {
                    setText(newText)
                    try {
                        setSelection(newText.length)
                    } catch (e: Exception) {
                        logException(e)
                    }
                } else {
                    setTextSafely(newText)
                }
            }
        }

    fun insertVariablePlaceholder(placeholder: VariablePlaceholder) {
        val position = selectionEnd.takeIf { it != -1 } ?: text.length
        val placeholderText = Variables.toPrettyPlaceholder(placeholder.variableKey)
        val span = VariableSpan(placeholderColor, placeholder.variableId)
        text.insert(position, placeholderText)
        text.setSpan(span, position, position + placeholderText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

}