package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import java.util.regex.Pattern

object TriggerShortcutManager {

    private const val REGEX = """(?:triggerShortcut|enqueueShortcut)\(/\*\[shortcut]\*/"([^"]+)"/\*\[/shortcut]\*/\);"""
    private val PATTERN = Pattern.compile(REGEX)

    fun getTriggeredShortcutIdsFromCode(code: String): List<ShortcutId> =
        buildList {
            val matcher = PATTERN.matcher(code)
            while (matcher.find()) {
                val shortcutId = matcher.group(1) ?: ""
                add(shortcutId)
            }
        }

    fun getCodeFromTriggeredShortcutIds(shortcutIds: List<ShortcutId>): String =
        shortcutIds.joinToString("\n") { shortcutId ->
            """enqueueShortcut(/*[shortcut]*/"$shortcutId"/*[/shortcut]*/);"""
        }
}
