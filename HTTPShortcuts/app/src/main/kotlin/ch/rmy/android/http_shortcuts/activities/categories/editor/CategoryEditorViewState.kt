package ch.rmy.android.http_shortcuts.activities.categories.editor

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior

data class CategoryEditorViewState(
    val dialogState: DialogState? = null,
    val toolbarTitle: Localizable,
    val categoryName: String,
    val categoryLayoutType: CategoryLayoutType,
    val categoryBackgroundType: CategoryBackgroundType,
    val categoryClickBehavior: ShortcutClickBehavior?,
    private val originalCategoryName: String,
    private val originalCategoryLayoutType: CategoryLayoutType,
    private val originalCategoryBackgroundType: CategoryBackgroundType,
    private val originalCategoryClickBehavior: ShortcutClickBehavior?,
) {
    val hasChanges: Boolean =
        categoryName != originalCategoryName ||
            categoryLayoutType != originalCategoryLayoutType ||
            categoryBackgroundType != originalCategoryBackgroundType ||
            categoryClickBehavior != originalCategoryClickBehavior

    val saveButtonVisible: Boolean =
        hasChanges && categoryName.isNotBlank()
}
