package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.framework.utils.spans.ColoredSpan

class JSShortcutSpan(color: Int, shortcutName: String) : ColoredSpan(color) {

    override val displayedText = "\"${shortcutName.replace("\"", "'")}\""
}
