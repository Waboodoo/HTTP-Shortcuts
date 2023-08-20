package ch.rmy.android.http_shortcuts.activities.categories

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable

@Stable
sealed class CategoriesDialogState {
    data class ContextMenu(
        val title: Localizable,
        val hideOptionVisible: Boolean,
        val showOptionVisible: Boolean,
        val placeOnHomeScreenOptionVisible: Boolean,
        val deleteOptionEnabled: Boolean,
    ) : CategoriesDialogState()

    object Deletion : CategoriesDialogState()

    object IconPicker : CategoriesDialogState()
}
