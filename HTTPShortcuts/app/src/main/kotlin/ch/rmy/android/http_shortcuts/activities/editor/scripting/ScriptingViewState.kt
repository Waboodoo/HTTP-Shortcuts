package ch.rmy.android.http_shortcuts.activities.editor.scripting

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.extensions.usesScriptingTestButton

@Stable
data class ScriptingViewState(
    val codeOnPrepare: String = "",
    val codeOnSuccess: String = "",
    val codeOnFailure: String = "",
    val isUndoButtonEnabled: Boolean = false,
    val shortcutExecutionType: ShortcutExecutionType = ShortcutExecutionType.HTTP,
) {
    val isTestButtonVisible: Boolean
        get() = shortcutExecutionType.usesScriptingTestButton

    val isTestButtonEnabled: Boolean
        get() = codeOnPrepare.isNotEmpty()
}
