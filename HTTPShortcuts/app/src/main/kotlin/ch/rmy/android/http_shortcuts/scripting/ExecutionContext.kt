package ch.rmy.android.http_shortcuts.scripting

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.variables.VariableManager
import org.liquidplayer.javascript.JSContext

class ExecutionContext(
    val jsContext: JSContext,
    val shortcutId: ShortcutId,
    val variableManager: VariableManager,
    val resultHandler: ResultHandler,
    val recursionDepth: Int,
    val dialogHandle: DialogHandle,
    val cleanupHandler: CleanupHandler,
    val onException: (Exception) -> Nothing,
) {
    fun throwException(exception: Exception): Nothing {
        onException(exception)
    }
}
