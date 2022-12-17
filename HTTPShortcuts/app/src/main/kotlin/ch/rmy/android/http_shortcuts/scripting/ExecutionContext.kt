package ch.rmy.android.http_shortcuts.scripting

import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.variables.VariableManager
import org.liquidplayer.javascript.JSContext

class ExecutionContext(
    val jsContext: JSContext,
    val shortcutId: ShortcutId,
    val variableManager: VariableManager,
    val resultHandler: ResultHandler,
    val recursionDepth: Int,
)
