package ch.rmy.android.http_shortcuts.media_controls

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MediaControlCatalogTest {

    private val first = MediaShortcutCategory("a", "First", listOf(MediaShortcutItem("1", "One", "")))
    private val second = MediaShortcutCategory("b", "Second", listOf(MediaShortcutItem("2", "Two", "")))
    private val third = MediaShortcutCategory("c", "Third", listOf(MediaShortcutItem("3", "Three", "")))
    private val empty = MediaShortcutCategory("empty", "Empty", emptyList())

    @Test
    fun `all categories use a flat catalog`() {
        val catalog = MediaControlCatalog.create(
            categories = listOf(first, second),
            includeAllCategories = true,
            selectedCategoryIds = emptySet(),
        )

        assertEquals(listOf(first, second), catalog.categories)
        assertFalse(catalog.useCategoryNavigation)
    }

    @Test
    fun `explicitly selecting every visible category uses a flat catalog`() {
        val catalog = MediaControlCatalog.create(
            categories = listOf(first, second),
            includeAllCategories = false,
            selectedCategoryIds = setOf(first.id, second.id),
        )

        assertFalse(catalog.useCategoryNavigation)
    }

    @Test
    fun `new category is included dynamically when all categories are enabled`() {
        val catalog = MediaControlCatalog.create(
            categories = listOf(first, second, third),
            includeAllCategories = true,
            selectedCategoryIds = setOf(first.id, second.id),
        )

        assertEquals(listOf(first, second, third), catalog.categories)
        assertFalse(catalog.useCategoryNavigation)
    }

    @Test
    fun `new category remains excluded from an explicit previous selection`() {
        val catalog = MediaControlCatalog.create(
            categories = listOf(first, second, third),
            includeAllCategories = false,
            selectedCategoryIds = setOf(first.id, second.id),
        )

        assertEquals(listOf(first, second), catalog.categories)
        assertTrue(catalog.useCategoryNavigation)
    }

    @Test
    fun `multiple selected categories use category navigation`() {
        val catalog = MediaControlCatalog.create(
            categories = listOf(first, second, third),
            includeAllCategories = false,
            selectedCategoryIds = setOf(first.id, third.id),
        )

        assertEquals(listOf(first, third), catalog.categories)
        assertTrue(catalog.useCategoryNavigation)
    }

    @Test
    fun `one playable category remains flat and empty categories are skipped`() {
        val catalog = MediaControlCatalog.create(
            categories = listOf(first, empty, second),
            includeAllCategories = false,
            selectedCategoryIds = setOf(first.id, empty.id),
        )

        assertEquals(listOf(first), catalog.categories)
        assertFalse(catalog.useCategoryNavigation)
    }

    @Test
    fun `stale selection falls back to all visible categories`() {
        val catalog = MediaControlCatalog.create(
            categories = listOf(first, second),
            includeAllCategories = false,
            selectedCategoryIds = setOf("deleted"),
        )

        assertEquals(listOf(first, second), catalog.categories)
        assertFalse(catalog.useCategoryNavigation)
    }

    @Test
    fun `catalog without playable shortcuts is empty`() {
        val catalog = MediaControlCatalog.create(
            categories = listOf(empty),
            includeAllCategories = true,
            selectedCategoryIds = emptySet(),
        )

        assertEquals(emptyList(), catalog.categories)
        assertFalse(catalog.useCategoryNavigation)
    }
}
