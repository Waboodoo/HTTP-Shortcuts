package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.framework.extensions.indexOfFirstOrNull
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.models.CategoryTabItem
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode

data class MainViewState(
    val toolbarTitle: String = "",
    val isLocked: Boolean,
    val categoryTabItems: List<CategoryTabItem>,
    val selectionMode: SelectionMode,
    val isInMovingMode: Boolean,
    val activeCategoryId: CategoryId,
    val dialogState: DialogState? = null,
) {
    val isRegularMenuButtonVisible
        get() = !isLocked

    val isUnlockButtonVisible
        get() = isLocked

    val isCreateButtonVisible
        get() = !isLocked && !isInMovingMode

    val isTabBarVisible
        get() = categoryTabItems.size > 1

    val isToolbarScrollable
        get() = categoryTabItems.size > 1

    val toolbarTitleLocalizable: Localizable
        get() = toolbarTitle.takeUnlessEmpty()?.toLocalizable() ?: StringResLocalizable(R.string.app_name)

    val activeCategoryIndex: Int
        get() = categoryTabItems.indexOfFirstOrNull { category ->
            category.categoryId == activeCategoryId
        }
            ?: 0
}
