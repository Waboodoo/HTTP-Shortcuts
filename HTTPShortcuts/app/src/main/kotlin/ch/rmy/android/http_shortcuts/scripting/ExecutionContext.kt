package ch.rmy.android.http_shortcuts.scripting

import android.content.Context
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.variables.VariableManager

class ExecutionContext(
    val context: Context,
    val shortcutId: ShortcutId,
    val variableManager: VariableManager,
    val recursionDepth: Int,
    private val callback: suspend (ActionRequest) -> ActionResult,
) {
    suspend fun sendRequest(actionRequest: ActionRequest): ActionResult =
        callback(actionRequest)
}
