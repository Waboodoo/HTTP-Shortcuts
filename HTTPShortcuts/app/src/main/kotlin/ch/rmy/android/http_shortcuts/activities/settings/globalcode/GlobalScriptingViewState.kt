package ch.rmy.android.http_shortcuts.activities.settings.globalcode

import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable

data class GlobalScriptingViewState(
    val dialogState: DialogState? = null,
    val globalCode: String = "",
    val saveButtonVisible: Boolean = false,
    val variables: List<Variable>? = null,
    val shortcuts: List<Shortcut>? = null,
)
