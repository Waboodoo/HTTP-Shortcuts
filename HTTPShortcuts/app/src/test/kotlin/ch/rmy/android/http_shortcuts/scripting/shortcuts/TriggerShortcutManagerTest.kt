package ch.rmy.android.http_shortcuts.scripting.shortcuts

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TriggerShortcutManagerTest {

    @Test
    fun testParseTriggerShortcutsCode() {
        val target = """
            triggerShortcut(/*[shortcut]*/"1234"/*[/shortcut]*/);
            triggerShortcut(/*[shortcut]*/"5678"/*[/shortcut]*/);
        """.trimIndent()

        val actual = TriggerShortcutManager.getTriggeredShortcutIdsFromCode(target)
        assertThat(actual.size, equalTo(2))
        assertThat(actual[0], equalTo("1234"))
        assertThat(actual[1], equalTo("5678"))
    }

    @Test
    fun testGenerateTriggerShortcutsCode() {
        val target = listOf(
            "1234",
            "5678",
        )
        val expected = """
            triggerShortcut(/*[shortcut]*/"1234"/*[/shortcut]*/);
            triggerShortcut(/*[shortcut]*/"5678"/*[/shortcut]*/);
        """.trimIndent()
        val actual = TriggerShortcutManager.getCodeFromTriggeredShortcutIds(target)
        assertThat(actual, equalTo(expected))
    }
}
