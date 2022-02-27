package ch.rmy.android.http_shortcuts.scripting.shortcuts

import java.util.regex.Pattern

object TriggerShortcutManager {

    private const val REGEX = """(?:triggerShortcut|enqueueShortcut)\(/\*\[shortcut]\*/"([^"]+)"/\*\[/shortcut]\*/\);"""
    private val PATTERN = Pattern.compile(REGEX)

    fun getTriggeredShortcutIdsFromCode(code: String): List<String> {
        val result = mutableListOf<String>()
        val matcher = PATTERN.matcher(code)
        while (matcher.find()) {
            val shortcutId = matcher.group(1) ?: ""
            result.add(shortcutId)
        }
        return result
    }

    fun getCodeFromTriggeredShortcutIds(shortcutIds: List<String>): String =
        shortcutIds.joinToString("\n") { shortcutId ->
            """enqueueShortcut(/*[shortcut]*/"$shortcutId"/*[/shortcut]*/);"""
        }
}
