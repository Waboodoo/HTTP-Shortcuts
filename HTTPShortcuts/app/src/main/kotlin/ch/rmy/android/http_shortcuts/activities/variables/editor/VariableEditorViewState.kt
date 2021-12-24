package ch.rmy.android.http_shortcuts.activities.variables.editor

import ch.rmy.android.framework.utils.localization.Localizable

data class VariableEditorViewState(
    val title: Localizable,
    val subtitle: Localizable,
    val titleInputVisible: Boolean,
    val variableKeyInputError: Localizable? = null,
    val variableKey: String = "",
    val variableTitle: String = "",
    val urlEncodeChecked: Boolean = false,
    val jsonEncodeChecked: Boolean = false,
    val allowShareChecked: Boolean = false,
) {
    val variableKeyErrorHighlighting
        get() = variableKeyInputError != null
}
