package ch.rmy.android.http_shortcuts.activities.categories.editor

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType

data class CategoryEditorViewState(
    val dialogState: DialogState? = null,
    val toolbarTitle: Localizable,
    val categoryName: String,
    val categoryLayoutType: CategoryLayoutType,
    val categoryBackgroundType: CategoryBackgroundType,
    private val originalCategoryName: String,
    private val originalCategoryLayoutType: CategoryLayoutType,
    private val originalCategoryBackgroundType: CategoryBackgroundType,
) {
    val hasChanges: Boolean =
        categoryName != originalCategoryName ||
            categoryLayoutType != originalCategoryLayoutType ||
            categoryBackgroundType != originalCategoryBackgroundType

    val saveButtonVisible: Boolean =
        hasChanges && categoryName.isNotBlank()
}
