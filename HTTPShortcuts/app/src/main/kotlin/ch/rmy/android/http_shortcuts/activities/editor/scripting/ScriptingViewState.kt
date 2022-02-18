package ch.rmy.android.http_shortcuts.activities.editor.scripting

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable

data class ScriptingViewState(
    val shortcuts: List<Shortcut> = emptyList(),
    val variables: List<Variable>? = null,
    val codeOnPrepare: String = "",
    val codeOnSuccess: String = "",
    val codeOnFailure: String = "",
    val codePrepareMinLines: Int = 6,
    val codePrepareHint: Localizable = Localizable.EMPTY,
    val codePrepareVisible: Boolean = false,
    val postRequestScriptingVisible: Boolean = false,
)
