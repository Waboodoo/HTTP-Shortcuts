package ch.rmy.android.http_shortcuts.activities.variablewidget.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Stable
data class SelectableShortcut(
    val shortcutId: ShortcutId,
    val name: String,
)
