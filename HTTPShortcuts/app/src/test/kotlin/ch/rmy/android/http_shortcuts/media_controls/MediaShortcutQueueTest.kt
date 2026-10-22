package ch.rmy.android.http_shortcuts.media_controls

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaShortcutQueueTest {

    @Test
    fun `selecting next and previous wraps around`() {
        val queue = MediaShortcutQueue()
        queue.update(listOf(item("1"), item("2"), item("3")))

        assertEquals("2", queue.selectNext()?.id)
        assertEquals("3", queue.selectNext()?.id)
        assertEquals("1", queue.selectNext()?.id)
        assertEquals("3", queue.selectPrevious()?.id)
    }

    @Test
    fun `updating items preserves the selection by ID`() {
        val queue = MediaShortcutQueue()
        queue.update(listOf(item("1"), item("2"), item("3")))
        queue.selectNext()

        assertEquals("2", queue.update(listOf(item("3"), item("2"), item("4")))?.id)
    }

    @Test
    fun `updating items selects the first item when selection disappeared`() {
        val queue = MediaShortcutQueue()
        queue.update(listOf(item("1"), item("2")))
        queue.selectNext()

        assertEquals("3", queue.update(listOf(item("3"), item("4")))?.id)
    }

    @Test
    fun `empty queue ignores navigation`() {
        val queue = MediaShortcutQueue()

        assertNull(queue.selectNext())
        assertNull(queue.selectPrevious())
    }

    private fun item(id: String) = MediaShortcutItem(
        id = id,
        name = "Shortcut $id",
        categoryName = "Category",
        description = "Description",
    )
}
