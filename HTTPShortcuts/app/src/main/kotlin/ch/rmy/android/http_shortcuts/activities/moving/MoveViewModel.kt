package ch.rmy.android.http_shortcuts.activities.moving

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.indexOfFirstOrNull
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.moving.models.CategorySectionItem
import ch.rmy.android.http_shortcuts.activities.moving.models.CategorySectionItem.CategorySectionId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Section
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.ids
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.MoveShortcuts.RESULT_SHORTCUTS_MOVED
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class MoveViewModel
@Inject
constructor(
    application: Application,
    private val categoryRepository: CategoryRepository,
    private val sectionRepository: SectionRepository,
    private val shortcutRepository: ShortcutRepository,
) : BaseViewModel<Unit, Unit>(application) {

    private val _categorySections = MutableStateFlow<List<CategorySectionItem>>(emptyList())
    val categorySections = _categorySections.asStateFlow()

    private var hasChanged = false

    override suspend fun initialize(data: Unit) {
        logInfo("Initialized MoveViewModel")
        viewModelScope.launch {
            combine(
                categoryRepository.observeCategories(),
                sectionRepository.observeSections()
                    .map { sections -> sections.groupBy { it.categoryId } },
                shortcutRepository.observeShortcuts()
                    .map { shortcuts -> shortcuts.groupBy { it.categoryId } },
            ) { categories, sectionsByCategoryId, shortcutsByCategoryId ->
                _categorySections.value = toCategorySectionItems(categories, sectionsByCategoryId, shortcutsByCategoryId)
            }
                .collect()
        }
    }

    private fun toCategorySectionItems(
        categories: List<Category>,
        sectionsByCategoryId: Map<CategoryId, List<Section>>,
        shortcutsByCategoryId: Map<CategoryId, List<Shortcut>>,
    ): List<CategorySectionItem> {
        return buildList {
            categories.forEach { category ->
                val sections = sectionsByCategoryId[category.id] ?: emptyList()
                val shortcuts = shortcutsByCategoryId[category.id] ?: emptyList()
                val validSectionIds = sections.ids()
                val shortcutsBySectionId = mutableMapOf<SectionId?, MutableList<Shortcut>>()
                shortcuts.forEach { shortcut ->
                    val sectionId = shortcut.sectionId?.takeIf { it in validSectionIds }
                    shortcutsBySectionId.getOrPut(sectionId, ::mutableListOf).add(shortcut)
                }

                (listOf(null) + sections).forEach { section ->
                    add(
                        CategorySectionItem(
                            id = CategorySectionId(category.id, section?.id),
                            categoryName = category.name,
                            sectionName = section?.name,
                            shortcuts = shortcutsBySectionId[section?.id]?.map(Shortcut::toShortcutPlaceholder) ?: emptyList(),
                        ),
                    )
                }
            }
        }
    }

    fun onShortcutMovedToShortcut(shortcutId: ShortcutId, targetShortcutId: ShortcutId) {
        val categorySections = _categorySections.value

        val categorySection1 = categorySections.firstOrNull { categorySection -> categorySection.contains(shortcutId) } ?: return
        val shortcut1 = categorySection1.shortcuts.find { it.id == shortcutId } ?: return

        logInfo("Moving shortcut to target shortcut's location")
        val categorySection2 = categorySections.firstOrNull { categorySection -> categorySection.contains(targetShortcutId) } ?: return
        val shortcut2Index = categorySection2.shortcuts.indexOfFirstOrNull { it.id == targetShortcutId } ?: return

        _categorySections.value = categorySections.map { categorySection ->
            if (categorySection.id == categorySection1.id && categorySection.id == categorySection2.id) {
                logInfo("Shortcuts are in same category section, swapping")
                categorySection.copy(
                    shortcuts = categorySection.shortcuts.swapped(shortcutId, targetShortcutId) { id },
                )
            } else if (categorySection.id == categorySection1.id) {
                logInfo("Removing shortcut from original category section")
                categorySection.copy(
                    shortcuts = categorySection.shortcuts.filter { it.id != shortcutId },
                )
            } else if (categorySection.id == categorySection2.id) {
                logInfo("Adding shortcut to target category section")
                categorySection.copy(
                    shortcuts = categorySection.shortcuts.toMutableList()
                        .apply {
                            add(shortcut2Index, shortcut1)
                        },
                )
            } else {
                categorySection
            }
        }
    }

    fun onShortcutMovedToCategory(shortcutId: ShortcutId, target: CategorySectionId) {
        val categorySections = _categorySections.value

        val categorySection1 = categorySections.firstOrNull { categorySection -> categorySection.contains(shortcutId) } ?: return
        val shortcut1 = categorySection1.shortcuts.find { it.id == shortcutId } ?: return

        logInfo("Moving shortcut to target category section")
        val categorySection1index = categorySections.indexOfFirstOrNull { categorySection -> categorySection.contains(shortcutId) } ?: return
        var categorySection2index = categorySections.indexOfFirstOrNull { it.id == target } ?: return

        if (categorySection1index == categorySection2index) {
            categorySection2index--
            if (categorySection2index < 0) {
                return
            }
        }

        _categorySections.value = categorySections.mapIndexed { index, categorySection ->
            when (index) {
                categorySection1index -> {
                    logInfo("Removing shortcut from original category section")
                    categorySection.copy(
                        shortcuts = categorySection.shortcuts.filter { it.id != shortcutId },
                    )
                }
                categorySection2index -> {
                    logInfo("Adding shortcut to target category section")
                    categorySection.copy(
                        shortcuts = categorySection.shortcuts.toMutableList()
                            .apply {
                                if (categorySection1index < categorySection2index) {
                                    add(0, shortcut1)
                                } else {
                                    add(shortcut1)
                                }
                            },
                    )
                }
                else -> {
                    categorySection
                }
            }
        }
    }

    fun onMoveEnded() = runAction {
        logInfo("Shortcut moving has ended, applying changes")
        withProgressTracking {
            shortcutRepository.moveShortcuts(
                _categorySections.value.associate { section ->
                    section.id.run { categoryId to sectionId } to section.shortcuts.map { it.id }
                },
            )
        }
        hasChanged = true
    }

    private fun CategorySectionItem.contains(shortcutId: ShortcutId) =
        shortcuts.any { it.id == shortcutId }

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        closeScreen(result = if (hasChanged) RESULT_SHORTCUTS_MOVED else null)
    }
}
