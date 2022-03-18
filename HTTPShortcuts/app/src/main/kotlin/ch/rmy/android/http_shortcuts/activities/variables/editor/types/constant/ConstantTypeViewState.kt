package ch.rmy.android.http_shortcuts.activities.variables.editor.types.constant

import ch.rmy.android.http_shortcuts.data.models.VariableModel

data class ConstantTypeViewState(
    val value: String,
    val variables: List<VariableModel>?,
)
