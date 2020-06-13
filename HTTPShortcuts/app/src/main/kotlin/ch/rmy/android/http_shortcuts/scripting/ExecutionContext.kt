package ch.rmy.android.http_shortcuts.scripting

import android.content.Context
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariableManager

class ExecutionContext(
    val context: Context,
    val shortcutId: String,
    val variableManager: VariableManager,
    val response: ShortcutResponse?,
    val responseError: ErrorResponse?,
    val recursionDepth: Int
)