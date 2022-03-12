package ch.rmy.android.http_shortcuts.activities.categories.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.CategoriesViewModel
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType

class GetLayoutTypeDialogUseCase {

    @CheckResult
    operator fun invoke(categoryId: String, viewModel: CategoriesViewModel) =
        DialogState.create {
            item(R.string.layout_type_linear_list) {
                viewModel.onLayoutTypeChanged(categoryId, CategoryLayoutType.LINEAR_LIST)
            }
                .item(R.string.layout_type_grid) {
                    viewModel.onLayoutTypeChanged(categoryId, CategoryLayoutType.GRID)
                }
                .build()
        }
}
