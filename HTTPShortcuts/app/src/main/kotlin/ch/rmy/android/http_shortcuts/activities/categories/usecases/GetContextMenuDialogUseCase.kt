package ch.rmy.android.http_shortcuts.activities.categories.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.CategoriesViewModel
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import javax.inject.Inject

class GetContextMenuDialogUseCase
@Inject
constructor() {

    @CheckResult
    operator fun invoke(
        categoryId: CategoryId,
        title: Localizable,
        hideOptionVisible: Boolean,
        showOptionVisible: Boolean,
        placeOnHomeScreenOptionVisible: Boolean,
        deleteOptionVisible: Boolean,
        viewModel: CategoriesViewModel,
    ) =
        createDialogState {
            title(title)
                .item(R.string.action_edit) {
                    viewModel.onEditCategoryOptionSelected(categoryId)
                }
                .runIf(showOptionVisible) {
                    item(R.string.action_show_category) {
                        viewModel.onCategoryVisibilityChanged(categoryId, hidden = false)
                    }
                }
                .runIf(hideOptionVisible) {
                    item(R.string.action_hide_category) {
                        viewModel.onCategoryVisibilityChanged(categoryId, hidden = true)
                    }
                }
                .runIf(placeOnHomeScreenOptionVisible) {
                    item(R.string.action_place_category) {
                        viewModel.onPlaceOnHomeScreenSelected(categoryId)
                    }
                }
                .runIf(deleteOptionVisible) {
                    item(R.string.action_delete) {
                        viewModel.onCategoryDeletionSelected(categoryId)
                    }
                }
                .build()
        }
}
