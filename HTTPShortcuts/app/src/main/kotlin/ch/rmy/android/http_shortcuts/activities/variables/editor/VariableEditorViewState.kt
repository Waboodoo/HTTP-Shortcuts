package ch.rmy.android.http_shortcuts.activities.variables.editor

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.activities.variables.editor.models.ShareSupport
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.VariableTypeViewState

@Stable
data class VariableEditorViewState(
    val dialogState: VariableEditorDialogState? = null,
    val dialogTitleVisible: Boolean,
    val dialogMessageVisible: Boolean,
    val variableKeyInputError: Localizable? = null,
    val variableKey: String = "",
    val dialogTitle: String = "",
    val dialogMessage: String = "",
    val urlEncodeChecked: Boolean = false,
    val jsonEncodeChecked: Boolean = false,
    val allowShareChecked: Boolean = false,
    val shareSupport: ShareSupport = ShareSupport.TEXT,
    val variableTypeViewState: VariableTypeViewState?,
    val excludeValueFromExports: Boolean = false,
    val excludeValueCheckboxVisible: Boolean,
) {
    val shareSupportVisible: Boolean
        get() = allowShareChecked
}
