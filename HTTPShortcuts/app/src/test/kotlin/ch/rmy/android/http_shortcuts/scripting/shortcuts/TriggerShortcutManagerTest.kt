package ch.rmy.android.http_shortcuts.scripting.shortcuts

import kotlin.test.Test
import kotlin.test.assertEquals

class TriggerShortcutManagerTest {

    @Test
    fun testParseTriggerShortcutsLegacyCode() {
        val target = """
            triggerShortcut(/*[shortcut]*/"1234"/*[/shortcut]*/);
            triggerShortcut(/*[shortcut]*/"5678"/*[/shortcut]*/);
        """.trimIndent()

        val actual = TriggerShortcutManager.getTriggeredShortcutIdsFromCode(target)
        assertEquals(
            listOf("1234", "5678"),
            actual,
        )
    }

    @Test
    fun testParseTriggerShortcutsCode() {
        val target = """
            enqueueShortcut(/*[shortcut]*/"1234"/*[/shortcut]*/);
            enqueueShortcut(/*[shortcut]*/"5678"/*[/shortcut]*/);
        """.trimIndent()

        val actual = TriggerShortcutManager.getTriggeredShortcutIdsFromCode(target)
        assertEquals(
            listOf("1234", "5678"),
            actual,
        )
    }

    @Test
    fun testGenerateTriggerShortcutsCode() {
        val target = listOf(
            "1234",
            "5678",
        )
        val expected = """
            enqueueShortcut(/*[shortcut]*/"1234"/*[/shortcut]*/);
            enqueueShortcut(/*[shortcut]*/"5678"/*[/shortcut]*/);
        """.trimIndent()
        val actual = TriggerShortcutManager.getCodeFromTriggeredShortcutIds(target)
        assertEquals(expected, actual)
    }
}
