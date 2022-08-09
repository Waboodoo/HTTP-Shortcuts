package ch.rmy.android.http_shortcuts.utils

import android.text.Editable
import androidx.core.text.getSpans
import ch.rmy.android.framework.utils.SimpleTextWatcher

class InvalidSpanRemover : SimpleTextWatcher {
    override fun afterTextChanged(s: Editable) {
        s
            .getSpans<LengthAwareSpan>()
            .reversed()
            .forEach { span ->
                val start = s.getSpanStart(span)
                val end = s.getSpanEnd(span)
                if (end - start != span.length) {
                    s.removeSpan(span)
                    s.delete(start, end)
                }
            }
    }
}
