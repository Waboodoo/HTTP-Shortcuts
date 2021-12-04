package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.utils.ColoredSpan

class JSVariableSpan(color: Int, val variableKey: String) : ColoredSpan(color) {

    override val displayedText = "\"$variableKey\""
}
