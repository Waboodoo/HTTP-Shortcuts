package ch.rmy.android.http_shortcuts.activities.moving

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.indexOfFirstOrNull
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.moving.models.CategoryItem
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.MoveShortcuts.RESULT_SHORTCUTS_MOVED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoveViewModel
@Inject
constructor(
    application: Application,
    private val categoryRepository: CategoryRepository,
    private val shortcutRepository: ShortcutRepository,
) : BaseViewModel<Unit, Unit>(application) {

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories = _categories.asStateFlow()

    private var hasChanged = false

    override suspend fun initialize(data: Unit) {
        logInfo("Initialized MoveViewModel")
        viewModelScope.launch {
            categoryRepository.getObservableCategories().collect {
                _categories.value = it.toCategoryItems()
            }
        }
    }

    private fun List<Category>.toCategoryItems() =
        map {
            CategoryItem(
                id = it.id,
                name = it.name,
                shortcuts = it.shortcuts.map(Shortcut::toShortcutPlaceholder)
            )
        }

    fun onShortcutMovedToShortcut(shortcutId: ShortcutId, targetShortcutId: ShortcutId) {
        val categories = _categories.value

        val category1 = categories.firstOrNull { category -> category.contains(shortcutId) } ?: return
        val shortcut1 = category1.shortcuts.find { it.id == shortcutId } ?: return

        logInfo("Moving shortcut to target shortcut's location")
        val category2 = categories.firstOrNull { category -> category.contains(targetShortcutId) } ?: return
        val shortcut2Index = category2.shortcuts.indexOfFirstOrNull { it.id == targetShortcutId } ?: return

        _categories.value = categories.map { category ->
            if (category.id == category1.id && category.id == category2.id) {
                logInfo("Shortcuts are in same category, swapping")
                category.copy(
                    shortcuts = category.shortcuts.swapped(shortcutId, targetShortcutId) { id }
                )
            } else if (category.id == category1.id) {
                logInfo("Removing shortcut from original category")
                category.copy(
                    shortcuts = category.shortcuts.filter { it.id != shortcutId }
                )
            } else if (category.id == category2.id) {
                logInfo("Adding shortcut to target category")
                category.copy(
                    shortcuts = category.shortcuts.toMutableList()
                        .apply {
                            add(shortcut2Index, shortcut1)
                        }
                )
            } else {
                category
            }
        }
    }

    fun onShortcutMovedToCategory(shortcutId: ShortcutId, targetCategoryId: CategoryId) {
        val categories = _categories.value

        val category1 = categories.firstOrNull { category -> category.contains(shortcutId) } ?: return
        val shortcut1 = category1.shortcuts.find { it.id == shortcutId } ?: return

        logInfo("Moving shortcut to target category")
        val category1index = categories.indexOfFirstOrNull { category -> category.contains(shortcutId) } ?: return
        var category2index = categories.indexOfFirstOrNull { it.id == targetCategoryId } ?: return

        if (category1index == category2index) {
            category2index--
            if (category2index < 0) {
                return
            }
        }

        _categories.value = categories.mapIndexed { index, category ->
            when (index) {
                category1index -> {
                    logInfo("Removing shortcut from original category")
                    category.copy(
                        shortcuts = category.shortcuts.filter { it.id != shortcutId }
                    )
                }
                category2index -> {
                    logInfo("Adding shortcut to target category")
                    category.copy(
                        shortcuts = category.shortcuts.toMutableList()
                            .apply {
                                if (category1index < category2index) {
                                    add(0, shortcut1)
                                } else {
                                    add(shortcut1)
                                }
                            }
                    )
                }
                else -> {
                    category
                }
            }
        }
    }

    fun onMoveEnded() = runAction {
        logInfo("Shortcut moving has ended, applying changes")
        withProgressTracking {
            shortcutRepository.moveShortcuts(
                _categories.value.associate { category -> category.id to category.shortcuts.map { it.id } }
            )
        }
        hasChanged = true
    }

    private fun CategoryItem.contains(shortcutId: ShortcutId) =
        shortcuts.any { it.id == shortcutId }

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        closeScreen(result = if (hasChanged) RESULT_SHORTCUTS_MOVED else null)
    }
}
