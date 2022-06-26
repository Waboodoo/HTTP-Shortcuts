package ch.rmy.android.http_shortcuts.activities.variables.editor

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState

data class VariableEditorViewState(
    val dialogState: DialogState? = null,
    val title: Localizable,
    val subtitle: Localizable,
    val dialogTitleVisible: Boolean,
    val dialogMessageVisible: Boolean,
    val variableKeyInputError: Localizable? = null,
    val variableKey: String = "",
    val variableTitle: String = "",
    val variableMessage: String = "",
    val urlEncodeChecked: Boolean = false,
    val jsonEncodeChecked: Boolean = false,
    val allowShareChecked: Boolean = false,
    val shareSupport: ShareSupport = ShareSupport.TEXT,
) {
    val variableKeyErrorHighlighting
        get() = variableKeyInputError != null

    val shareSupportVisible: Boolean
        get() = allowShareChecked

    enum class ShareSupport(val text: Boolean = false, val title: Boolean = false) {
        TEXT(text = true),
        TITLE(title = true),
        TITLE_AND_TEXT(text = true, title = true),
    }
}
