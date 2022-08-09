package ch.rmy.android.http_shortcuts.variables

import androidx.annotation.ColorInt
import ch.rmy.android.framework.utils.spans.ColoredSpan
import ch.rmy.android.http_shortcuts.utils.LengthAwareSpan

class JSVariableSpan(
    @ColorInt color: Int,
    val variableKey: String,
    override val length: Int,
) : ColoredSpan(color), LengthAwareSpan {

    override val displayedText = "\"$variableKey\""
}
