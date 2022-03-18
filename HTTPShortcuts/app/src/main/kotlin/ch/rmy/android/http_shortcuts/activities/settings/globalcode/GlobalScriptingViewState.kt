package ch.rmy.android.http_shortcuts.activities.settings.globalcode

import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel

data class GlobalScriptingViewState(
    val dialogState: DialogState? = null,
    val globalCode: String = "",
    val saveButtonVisible: Boolean = false,
    val variables: List<VariableModel>? = null,
    val shortcuts: List<ShortcutModel>? = null,
)
