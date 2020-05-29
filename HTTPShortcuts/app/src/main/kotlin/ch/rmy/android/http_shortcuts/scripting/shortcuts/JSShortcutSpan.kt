package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.http_shortcuts.utils.ColoredSpan

class JSShortcutSpan(color: Int, val shortcutName: String) : ColoredSpan(color) {

    protected override val displayedText = "\"${shortcutName.replace("\"", "'")}\""

}