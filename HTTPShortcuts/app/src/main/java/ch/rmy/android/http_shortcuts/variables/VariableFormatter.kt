package ch.rmy.android.http_shortcuts.variables

import android.graphics.Typeface
import android.text.Editable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.Destroyable
import ch.rmy.android.http_shortcuts.utils.SimpleTextWatcher
import ch.rmy.android.http_shortcuts.utils.color

class VariableFormatter private constructor(private val textView: TextView, private val variables: List<Variable>) : SimpleTextWatcher(), Destroyable {

    private val highlightColor by lazy {
        color(textView.context, R.color.variable)
    }

    override fun afterTextChanged(s: Editable) {
        textView.removeTextChangedListener(this)
        clearFormatting(s)
        val matcher = Variables.match(s)
        var previousEnd = 0
        while (matcher.find()) {
            if (matcher.start() < previousEnd) {
                continue
            }
            val variableName = s.subSequence(matcher.start() + Variables.PREFIX_LENGTH, matcher.end() - Variables.SUFFIX_LENGTH).toString()
            if (isValidVariable(variableName)) {
                format(s, matcher.start(), matcher.end())
                previousEnd = matcher.end()
            }
        }
        textView.addTextChangedListener(this)
    }

    private fun isValidVariable(variableName: String) =
            variables.any { it.isValid && variableName == it.key }

    private fun clearFormatting(s: Editable) {
        for (span in s.getSpans(0, s.length + 1, ForegroundColorSpan::class.java)) {
            s.removeSpan(span)
        }
        for (span in s.getSpans(0, s.length + 1, StyleSpan::class.java)) {
            s.removeSpan(span)
        }
    }

    private fun format(s: Editable, start: Int, end: Int) {
        s.setSpan(ForegroundColorSpan(highlightColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        s.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    override fun destroy() {
        textView.removeTextChangedListener(this)
    }

    companion object {

        fun bind(textView: TextView, variables: List<Variable>): Destroyable {
            val formatter = VariableFormatter(textView, variables)
            formatter.afterTextChanged(textView.editableText)
            return formatter
        }
    }
}
