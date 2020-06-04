package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.http_shortcuts.utils.ColoredSpan

class JSShortcutSpan(color: Int, shortcutName: String) : ColoredSpan(color) {

    override val displayedText = "\"${shortcutName.replace("\"", "'")}\""

}