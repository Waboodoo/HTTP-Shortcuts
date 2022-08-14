package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder
import ch.rmy.android.http_shortcuts.utils.InvalidSpanRemover
import javax.inject.Inject

class VariableEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.autoCompleteTextViewStyle,
) : AppCompatAutoCompleteTextView(context, attrs, defStyleAttr) {

    @Inject
    lateinit var variablePlaceholderProvider: VariablePlaceholderProvider

    init {
        getApplicationComponent().inject(this)
        addTextChangedListener(InvalidSpanRemover())
    }

    private val placeholderColor by lazy(LazyThreadSafetyMode.NONE) {
        color(context, R.color.variable)
    }

    private val maxLength: Int? =
        attrs?.getAttributeIntValue(android.R.attr.maxLength, -1).takeUnless { it == -1 }

    var rawString: String
        get() = Variables.variableSpansToRawPlaceholders(text)
        set(value) {
            val newText = Variables.rawPlaceholdersToVariableSpans(value, variablePlaceholderProvider, placeholderColor)

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
        val placeholderText = SpannableString(Variables.toPrettyPlaceholder(placeholder.variableKey))

        if (maxLength != null && position + placeholderText.length > maxLength) {
            context.showToast(context.getString(R.string.error_text_too_long_for_variable, maxLength), long = true)
            return
        }

        val span = VariableSpan(placeholderColor, placeholder.variableId, length = placeholderText.length)
        placeholderText.setSpan(span, 0, placeholderText.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        text.insert(position, placeholderText)
    }
}
