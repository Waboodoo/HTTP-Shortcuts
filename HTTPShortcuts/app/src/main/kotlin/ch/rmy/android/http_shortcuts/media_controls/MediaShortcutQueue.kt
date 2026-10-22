package ch.rmy.android.http_shortcuts.media_controls

import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

internal data class MediaShortcutItem(
    val id: ShortcutId,
    val name: String,
    val description: String,
)

internal data class MediaShortcutCategory(
    val id: CategoryId,
    val name: String,
    val shortcuts: List<MediaShortcutItem>,
)

internal data class MediaControlCatalog(
    val categories: List<MediaShortcutCategory>,
    val useCategoryNavigation: Boolean,
) {
    companion object {
        fun create(
            categories: List<MediaShortcutCategory>,
            includeAllCategories: Boolean,
            selectedCategoryIds: Set<CategoryId>,
        ): MediaControlCatalog {
            val selectedCategories = if (includeAllCategories) {
                categories
            } else {
                categories.filter { it.id in selectedCategoryIds }
                    .ifEmpty { categories }
            }
            val allCategoriesSelected = includeAllCategories || selectedCategories.size == categories.size
            val playableCategories = selectedCategories.filter { it.shortcuts.isNotEmpty() }
            return MediaControlCatalog(
                categories = playableCategories,
                useCategoryNavigation = !allCategoriesSelected && playableCategories.size > 1,
            )
        }
    }
}

internal sealed interface MediaControlItem {
    data class Shortcut(val shortcut: MediaShortcutItem) : MediaControlItem

    data object ChangeCategory : MediaControlItem
}

internal class MediaShortcutQueue {

    private var catalog = MediaControlCatalog(emptyList(), useCategoryNavigation = false)
    private var selectedCategoryIndex = 0
    private var selectedItemIndex = 0

    val current: MediaControlItem?
        get() = items.getOrNull(selectedItemIndex)

    val currentCategory: MediaShortcutCategory?
        get() = if (catalog.useCategoryNavigation) {
            catalog.categories.getOrNull(selectedCategoryIndex)
        } else {
            val selectedShortcutId = (current as? MediaControlItem.Shortcut)?.shortcut?.id
            catalog.categories.firstOrNull { category ->
                category.shortcuts.any { it.id == selectedShortcutId }
            }
        }

    val itemCount: Int
        get() = items.size

    val selectedItemNumber: Int
        get() = (selectedItemIndex + 1).takeIf { items.isNotEmpty() } ?: 0

    private val items: List<MediaControlItem>
        get() = if (catalog.useCategoryNavigation) {
            buildList {
                add(MediaControlItem.ChangeCategory)
                addAll(
                    catalog.categories
                        .getOrNull(selectedCategoryIndex)
                        ?.shortcuts
                        .orEmpty()
                        .map(MediaControlItem::Shortcut),
                )
            }
        } else {
            catalog.categories.flatMap { category ->
                category.shortcuts.map(MediaControlItem::Shortcut)
            }
        }

    fun replace(catalog: MediaControlCatalog) {
        val previousItem = current
        val previousCategoryId = currentCategory?.id
        val previousItemIndex = selectedItemIndex

        this.catalog = catalog
        selectedCategoryIndex = previousCategoryId
            ?.let { id -> catalog.categories.indexOfFirst { it.id == id } }
            ?.takeIf { it >= 0 }
            ?: selectedCategoryIndex.coerceAtMost(catalog.categories.lastIndex.coerceAtLeast(0))

        selectedItemIndex = when (previousItem) {
            MediaControlItem.ChangeCategory -> 0
            is MediaControlItem.Shortcut -> items.indexOfFirst { item ->
                (item as? MediaControlItem.Shortcut)?.shortcut?.id == previousItem.shortcut.id
            }.takeIf { it >= 0 }
            null -> null
        } ?: previousItemIndex.coerceAtMost(items.lastIndex.coerceAtLeast(0))
    }

    fun next(): MediaControlItem? {
        if (items.isEmpty()) {
            return null
        }
        selectedItemIndex = (selectedItemIndex + 1) % items.size
        return current
    }

    fun previous(): MediaControlItem? {
        if (items.isEmpty()) {
            return null
        }
        selectedItemIndex = (selectedItemIndex - 1 + items.size) % items.size
        return current
    }

    fun changeCategory(): Boolean {
        if (!catalog.useCategoryNavigation || current != MediaControlItem.ChangeCategory) {
            return false
        }
        selectedCategoryIndex = (selectedCategoryIndex + 1) % catalog.categories.size
        return true
    }
}
