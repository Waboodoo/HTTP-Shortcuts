package ch.rmy.android.http_shortcuts.activities.main.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.extensions.mapFor
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.ShortcutListViewModel

class GetMoveToCategoryDialogUseCase {

    @CheckResult
    operator fun invoke(shortcutId: String, categoryOptions: List<CategoryOption>, viewModel: ShortcutListViewModel) =
        DialogState.create {
            title(R.string.title_move_to_category)
                .mapFor(categoryOptions) { categoryOption ->
                    item(name = categoryOption.name) {
                        viewModel.onMoveTargetCategorySelected(shortcutId, categoryOption.categoryId)
                    }
                }
                .build()
        }

    data class CategoryOption(val categoryId: String, val name: String)
}
