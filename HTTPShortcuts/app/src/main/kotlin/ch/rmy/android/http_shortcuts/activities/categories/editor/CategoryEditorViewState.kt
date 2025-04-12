package ch.rmy.android.http_shortcuts.activities.categories.editor

import android.graphics.Color
import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.categories.editor.models.CategoryBackground
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior

@Stable
data class CategoryEditorViewState(
    val dialogState: CategoryEditorDialogState? = null,
    val categoryName: String,
    val categoryLayoutType: CategoryLayoutType,
    val categoryBackgroundType: CategoryBackgroundType,
    val categoryClickBehavior: ShortcutClickBehavior?,
    val scale: Float,
    private val originalCategoryName: String = categoryName,
    private val originalCategoryLayoutType: CategoryLayoutType = categoryLayoutType,
    private val originalCategoryBackgroundType: CategoryBackgroundType = categoryBackgroundType,
    private val originalCategoryClickBehavior: ShortcutClickBehavior? = categoryClickBehavior,
    private val originalScale: Float = scale,
) {
    val hasChanges: Boolean =
        categoryName != originalCategoryName ||
            categoryLayoutType != originalCategoryLayoutType ||
            categoryBackgroundType != originalCategoryBackgroundType ||
            categoryClickBehavior != originalCategoryClickBehavior ||
            scale != originalScale

    val saveButtonEnabled: Boolean =
        hasChanges && categoryName.isNotBlank()

    val colorButtonVisible: Boolean
        get() = categoryBackgroundType is CategoryBackgroundType.Color

    val backgroundColor: Int
        get() = (categoryBackgroundType as? CategoryBackgroundType.Color)?.color ?: Color.WHITE

    val backgroundColorAsText: String
        get() = (categoryBackgroundType as? CategoryBackgroundType.Color)?.getHexString() ?: ""

    val categoryBackground: CategoryBackground
        get() = when (categoryBackgroundType) {
            is CategoryBackgroundType.Default -> CategoryBackground.DEFAULT
            is CategoryBackgroundType.Color -> CategoryBackground.COLOR
        }
}
