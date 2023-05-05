package ch.rmy.android.http_shortcuts.activities.editor.shortcuts.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Stable
data class ShortcutListItemId(val shortcutId: ShortcutId, val id: String) {
    override fun toString(): String =
        "$shortcutId.$id"

    companion object {
        fun fromString(string: String) =
            string.split(".").let {
                ShortcutListItemId(it[0], it[1])
            }
    }
}
