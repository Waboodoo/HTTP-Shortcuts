package ch.rmy.android.http_shortcuts.variables

import androidx.annotation.ColorInt
import ch.rmy.android.framework.utils.spans.ColoredSpan

class JSVariableSpan(@ColorInt color: Int, val variableKey: String) : ColoredSpan(color) {

    override val displayedText = "\"$variableKey\""
}
