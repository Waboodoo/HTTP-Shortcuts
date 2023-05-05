package ch.rmy.android.http_shortcuts.activities.editor.shortcuts.models

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Stable
data class ShortcutListItem(
    val id: ShortcutListItemId,
    val name: Localizable,
    val icon: ShortcutIcon,
)
