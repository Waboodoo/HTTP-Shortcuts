package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.framework.utils.spans.ColoredSpan
import ch.rmy.android.http_shortcuts.utils.LengthAwareSpan

class JSShortcutSpan(
    color: Int,
    shortcutName: String,
    override val length: Int,
) : ColoredSpan(color), LengthAwareSpan {

    override val displayedText = "\"${shortcutName.replace("\"", "'")}\""
}
