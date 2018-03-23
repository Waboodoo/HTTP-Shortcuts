package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.AttributeSet
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.color
import java.util.*

class VariableEditText : EditText {

    private val variableColor by lazy {
        color(context, R.color.variable)
    }

    lateinit var variables: List<Variable>

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setText(text: CharSequence?, type: BufferType?) {
        val processedText = text?.let { replaceRawVariablePlaceholders(text) }
        super.setText(processedText, type)
        setSelection(processedText?.length ?: 0)
    }

    private fun replaceRawVariablePlaceholders(text: CharSequence): CharSequence {
        val builder = SpannableStringBuilder(text)
        val matcher = Variables.match(text)

        val replacements = LinkedList<Replacement>()
        while (matcher.find()) {
            val variableKey = matcher.group(1)
            if (isValidVariable(variableKey)) {
                replacements.add(Replacement(matcher.start(), matcher.end(), variableKey))
            }
        }

        val it = replacements.descendingIterator()
        while (it.hasNext()) {
            val replacement = it.next()
            val placeholderText = Variables.toPrettyPlaceholder(replacement.variableKey)
            val span = VariableSpan(variableColor, replacement.variableKey)
            builder.replace(replacement.startIndex, replacement.endIndex, placeholderText)
            builder.setSpan(span, replacement.startIndex, replacement.startIndex + placeholderText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return builder
    }

    private fun isValidVariable(variableKey: String) =
            variables.any { it.isValid && variableKey == it.key }

    private class Replacement(internal val startIndex: Int, internal val endIndex: Int, internal val variableKey: String)

    val rawString: String
        get() {
            val builder = StringBuilder(text)
            text.getSpans(0, text.length, VariableSpan::class.java)
                    .sortedBy {
                        text.getSpanStart(it)
                    }
                    .forEach { span ->
                        val start = text.getSpanStart(span)
                        val end = text.getSpanEnd(span)
                        val replacement = Variables.toRawPlaceholder(span.variableKey)
                        builder.replace(start, end, replacement)
                    }

            return builder.toString()
        }

    fun insertVariablePlaceholder(variableKey: String) {
        val selectionEnd = selectionEnd
        val position = if (selectionEnd != -1) selectionEnd else text.length
        val placeholderText = Variables.toPrettyPlaceholder(variableKey)
        val span = VariableSpan(variableColor, variableKey)
        text.insert(position, placeholderText)
        text.setSpan(span, position, position + placeholderText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

}