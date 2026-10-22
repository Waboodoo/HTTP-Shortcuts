package ch.rmy.android.http_shortcuts.media_controls

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class MediaShortcutQueueTest {

    private val first = MediaShortcutItem("1", "First", "")
    private val second = MediaShortcutItem("2", "Second", "")
    private val third = MediaShortcutItem("3", "Third", "")
    private val firstCategory = MediaShortcutCategory("a", "First category", listOf(first, second))
    private val secondCategory = MediaShortcutCategory("b", "Second category", listOf(third))

    private fun flatCatalog(vararg shortcuts: MediaShortcutItem) =
        MediaControlCatalog(
            categories = listOf(MediaShortcutCategory("a", "Category", shortcuts.toList())),
            useCategoryNavigation = false,
        )

    @Test
    fun `next and previous wrap around`() {
        val queue = MediaShortcutQueue()
        queue.replace(flatCatalog(first, second, third))

        assertEquals(MediaControlItem.Shortcut(second), queue.next())
        assertEquals(MediaControlItem.Shortcut(third), queue.next())
        assertEquals(MediaControlItem.Shortcut(first), queue.next())
        assertEquals(MediaControlItem.Shortcut(third), queue.previous())
    }

    @Test
    fun `selection is preserved when items are replaced`() {
        val queue = MediaShortcutQueue()
        queue.replace(flatCatalog(first, second, third))
        queue.next()

        queue.replace(flatCatalog(third, second, first))

        assertEquals(MediaControlItem.Shortcut(second), queue.current)
    }

    @Test
    fun `current item data is updated when items are replaced`() {
        val queue = MediaShortcutQueue()
        queue.replace(flatCatalog(first, second))
        queue.next()
        val updatedSecond = second.copy(name = "Updated", description = "New description")

        queue.replace(flatCatalog(first, updatedSecond))

        assertEquals(MediaControlItem.Shortcut(updatedSecond), queue.current)
    }

    @Test
    fun `selection moves to nearest item when selected item is removed`() {
        val queue = MediaShortcutQueue()
        queue.replace(flatCatalog(first, second, third))
        queue.next()
        queue.next()

        queue.replace(flatCatalog(first, second))

        assertEquals(MediaControlItem.Shortcut(second), queue.current)
    }

    @Test
    fun `replacement starts with first item after queue becomes empty`() {
        val queue = MediaShortcutQueue()
        queue.replace(flatCatalog(first, second, third))
        queue.next()
        queue.next()

        queue.replace(flatCatalog())
        queue.replace(flatCatalog(first, second))

        assertEquals(MediaControlItem.Shortcut(first), queue.current)
    }

    @Test
    fun `navigation in single-item queue keeps selection`() {
        val queue = MediaShortcutQueue()
        queue.replace(flatCatalog(first))

        assertEquals(MediaControlItem.Shortcut(first), queue.next())
        assertEquals(MediaControlItem.Shortcut(first), queue.previous())
    }

    @Test
    fun `empty queue has no selection`() {
        val queue = MediaShortcutQueue()

        assertNull(queue.next())
        assertNull(queue.previous())
    }

    @Test
    fun `category navigation starts with switch item and keeps it selected`() {
        val queue = MediaShortcutQueue()
        queue.replace(MediaControlCatalog(listOf(firstCategory, secondCategory), useCategoryNavigation = true))

        assertEquals(MediaControlItem.ChangeCategory, queue.current)
        assertEquals(firstCategory, queue.currentCategory)
        assertEquals(3, queue.itemCount)

        queue.changeCategory()

        assertEquals(MediaControlItem.ChangeCategory, queue.current)
        assertEquals(secondCategory, queue.currentCategory)
        assertEquals(2, queue.itemCount)
    }

    @Test
    fun `category switching only works while switch item is selected`() {
        val queue = MediaShortcutQueue()
        queue.replace(MediaControlCatalog(listOf(firstCategory, secondCategory), useCategoryNavigation = true))
        queue.next()

        assertFalse(queue.changeCategory())
        assertEquals(firstCategory, queue.currentCategory)
    }

    @Test
    fun `switch item and active category survive live catalog updates`() {
        val queue = MediaShortcutQueue()
        queue.replace(MediaControlCatalog(listOf(firstCategory, secondCategory), useCategoryNavigation = true))
        queue.changeCategory()
        val renamedCategory = secondCategory.copy(name = "Renamed category")

        queue.replace(MediaControlCatalog(listOf(firstCategory, renamedCategory), useCategoryNavigation = true))

        assertEquals(MediaControlItem.ChangeCategory, queue.current)
        assertEquals(renamedCategory, queue.currentCategory)
    }

    @Test
    fun `removing the active category selects the nearest remaining category`() {
        val queue = MediaShortcutQueue()
        queue.replace(MediaControlCatalog(listOf(firstCategory, secondCategory), useCategoryNavigation = true))
        queue.changeCategory()

        queue.replace(
            MediaControlCatalog(
                listOf(firstCategory, MediaShortcutCategory("c", "Third category", listOf(third))),
                useCategoryNavigation = true,
            ),
        )

        assertEquals(MediaControlItem.ChangeCategory, queue.current)
        assertEquals("c", queue.currentCategory?.id)
    }

    @Test
    fun `uneven large catalog cycles through every category and shortcut`() {
        val shortcutCounts = listOf(1, 5, 10, 20, 1, 5, 10, 20, 1, 20)
        val categories = shortcutCounts.mapIndexed { categoryIndex, shortcutCount ->
            MediaShortcutCategory(
                id = "category-$categoryIndex",
                name = "Category $categoryIndex",
                shortcuts = List(shortcutCount) { shortcutIndex ->
                    MediaShortcutItem(
                        id = "shortcut-$categoryIndex-$shortcutIndex",
                        name = "Shortcut $categoryIndex-$shortcutIndex",
                        description = "",
                    )
                },
            )
        }
        val queue = MediaShortcutQueue()
        queue.replace(MediaControlCatalog(categories, useCategoryNavigation = true))

        categories.forEach { category ->
            assertEquals(category, queue.currentCategory)
            assertEquals(MediaControlItem.ChangeCategory, queue.current)
            assertEquals(category.shortcuts.size + 1, queue.itemCount)

            category.shortcuts.forEach { shortcut ->
                assertEquals(MediaControlItem.Shortcut(shortcut), queue.next())
            }
            assertEquals(MediaControlItem.ChangeCategory, queue.next())
            queue.changeCategory()
        }

        assertEquals(categories.first(), queue.currentCategory)
        assertEquals(MediaControlItem.ChangeCategory, queue.current)
    }
}
