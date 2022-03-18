package ch.rmy.android.http_shortcuts.activities.editor.scripting

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel

data class ScriptingViewState(
    val shortcuts: List<ShortcutModel>? = null,
    val variables: List<VariableModel>? = null,
    val codeOnPrepare: String = "",
    val codeOnSuccess: String = "",
    val codeOnFailure: String = "",
    val codePrepareMinLines: Int = 6,
    val codePrepareHint: Localizable = Localizable.EMPTY,
    val codePrepareVisible: Boolean = false,
    val postRequestScriptingVisible: Boolean = false,
)
