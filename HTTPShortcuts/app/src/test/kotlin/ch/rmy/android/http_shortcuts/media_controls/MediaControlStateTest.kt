package ch.rmy.android.http_shortcuts.media_controls

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MediaControlStateTest {

    private val first = MediaShortcutItem("1", "First", "")
    private val second = MediaShortcutItem("2", "Second", "")
    private fun catalog(vararg shortcuts: MediaShortcutItem) = MediaControlCatalog(
        categories = listOf(MediaShortcutCategory("a", "Category", shortcuts.toList())),
        useCategoryNavigation = false,
    )

    @Test
    fun `commands are buffered until shortcuts are loaded`() {
        val state = MediaControlState(maxPendingCommands = 3)

        assertEquals(emptyList(), state.accept(MediaControlCommand.Next))
        assertEquals(emptyList(), state.accept(MediaControlCommand.Trigger))

        assertEquals(
            listOf(MediaControlCommand.Next, MediaControlCommand.Trigger),
            state.replaceCatalog(catalog(first, second)),
        )
    }

    @Test
    fun `pending command buffer is bounded and preserves order`() {
        val state = MediaControlState(maxPendingCommands = 2)

        state.accept(MediaControlCommand.Next)
        state.accept(MediaControlCommand.Previous)
        state.accept(MediaControlCommand.Trigger)

        assertEquals(
            listOf(MediaControlCommand.Next, MediaControlCommand.Previous),
            state.replaceCatalog(catalog(first)),
        )
    }

    @Test
    fun `commands are returned immediately after initialization`() {
        val state = MediaControlState(maxPendingCommands = 1)
        state.replaceCatalog(MediaControlCatalog(emptyList(), useCategoryNavigation = false))

        assertEquals(listOf(MediaControlCommand.Trigger), state.accept(MediaControlCommand.Trigger))
    }

    @Test
    fun `selection and execution state are maintained independently`() {
        val state = MediaControlState(maxPendingCommands = 1)
        state.replaceCatalog(catalog(first, second))

        state.next()
        state.setExecuting(true)

        assertEquals(MediaControlItem.Shortcut(second), state.current)
        assertTrue(state.isExecuting)

        state.previous()
        state.setExecuting(false)

        assertEquals(MediaControlItem.Shortcut(first), state.current)
        assertFalse(state.isExecuting)
    }
}
