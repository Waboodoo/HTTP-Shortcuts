package ch.rmy.android.http_shortcuts.scripting.shortcuts

import java.util.regex.Pattern

object TriggerShortcutManager {

    private const val REGEX = """triggerShortcut\(/\*\[shortcut]\*/"([^"]+)"/\*\[/shortcut]\*/\);"""
    private val PATTERN = Pattern.compile(REGEX)

    fun getTriggeredShortcutsFromCode(code: String): List<TriggeredShortcut> {
        val result = mutableListOf<TriggeredShortcut>()
        val matcher = PATTERN.matcher(code)
        while (matcher.find()) {
            val shortcutId = matcher.group(1) ?: ""
            result.add(TriggeredShortcut(shortcutId))
        }
        return result
    }

    fun getCodeFromTriggeredShortcuts(shortcuts: List<TriggeredShortcut>): String =
        shortcuts.joinToString("\n") {
            """triggerShortcut(/*[shortcut]*/"${it.shortcutId}"/*[/shortcut]*/);"""
        }

    class TriggeredShortcut(val shortcutId: String)

}
