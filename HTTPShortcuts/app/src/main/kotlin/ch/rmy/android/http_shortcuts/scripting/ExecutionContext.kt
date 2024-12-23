package ch.rmy.android.http_shortcuts.scripting

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.scripting.ScriptingEngine

class ExecutionContext(
    val scriptingEngine: ScriptingEngine,
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
