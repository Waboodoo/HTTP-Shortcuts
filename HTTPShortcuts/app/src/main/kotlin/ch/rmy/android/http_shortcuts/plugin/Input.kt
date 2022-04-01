package ch.rmy.android.http_shortcuts.plugin

import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
data class Input
@JvmOverloads
constructor(
    @field:TaskerInputField("${FIELD_PREFIX}shortcut_id")
    val shortcutId: ShortcutId = "",
    @field:TaskerInputField("${FIELD_PREFIX}shortcut_name")
    val shortcutName: String = "",
) {
    companion object {
        const val FIELD_PREFIX = "ch.rmy.android.http_shortcuts."
    }
}
