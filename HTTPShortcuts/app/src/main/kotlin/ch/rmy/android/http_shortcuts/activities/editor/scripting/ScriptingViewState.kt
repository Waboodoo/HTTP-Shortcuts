package ch.rmy.android.http_shortcuts.activities.editor.scripting

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType

@Stable
data class ScriptingViewState(
    val codeOnPrepare: String = "",
    val codeOnSuccess: String = "",
    val codeOnFailure: String = "",
    val isUndoButtonEnabled: Boolean = false,
    val shortcutExecutionType: ShortcutExecutionType = ShortcutExecutionType.APP,
) {
    val isTestButtonVisible: Boolean
        get() = shortcutExecutionType == ShortcutExecutionType.SCRIPTING

    val isTestButtonEnabled: Boolean
        get() = codeOnPrepare.isNotEmpty()
}
