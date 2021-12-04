package ch.rmy.android.http_shortcuts.plugin

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
data class Input
@JvmOverloads
constructor(
    @field:TaskerInputField("${FIELD_PREFIX}shortcut_id")
    val shortcutId: String = "",
    @field:TaskerInputField("${FIELD_PREFIX}shortcut_name")
    val shortcutName: String = "",
) {
    companion object {
        const val FIELD_PREFIX = "ch.rmy.android.http_shortcuts."
    }
}
