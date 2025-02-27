package ch.rmy.android.http_shortcuts.activities.categories

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Stable
sealed class CategoriesDialogState {
    data class ContextMenu(
        val title: Localizable,
        val hideOptionVisible: Boolean,
        val showOptionVisible: Boolean,
        val placeOnHomeScreenOptionVisible: Boolean,
        val hideOptionEnabled: Boolean,
        val deleteOptionEnabled: Boolean,
    ) : CategoriesDialogState()

    data class Deletion(
        val title: String,
    ) : CategoriesDialogState()

    data class IconPicker(
        val currentIcon: ShortcutIcon.BuiltInIcon?,
        val suggestionBase: String?,
    ) : CategoriesDialogState()
}
