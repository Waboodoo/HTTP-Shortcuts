package ch.rmy.android.http_shortcuts.activities.categories.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.CategoriesViewModel
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType

class GetBackgroundTypeDialogUseCase {

    @CheckResult
    operator fun invoke(categoryId: String, viewModel: CategoriesViewModel) =
        DialogState.create {
            item(R.string.category_background_type_white) {
                viewModel.onBackgroundTypeChanged(categoryId, CategoryBackgroundType.WHITE)
            }
                .item(R.string.category_background_type_black) {
                    viewModel.onBackgroundTypeChanged(categoryId, CategoryBackgroundType.BLACK)
                }
                .item(R.string.category_background_type_wallpaper) {
                    viewModel.onBackgroundTypeChanged(categoryId, CategoryBackgroundType.WALLPAPER)
                }
                .build()
        }
}
