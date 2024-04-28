package ch.rmy.android.http_shortcuts.activities.main.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Stable
data class RecoveryInfo(
    val shortcutName: String,
    val shortcutId: ShortcutId?,
    val categoryId: CategoryId,
)
