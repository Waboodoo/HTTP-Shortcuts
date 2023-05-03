package ch.rmy.android.http_shortcuts.activities.variables.editor

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ColorTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ColorTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ConstantTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ConstantTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.DateTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.DateTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.SelectTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.SelectTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.SliderTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.SliderTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TextTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TextTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TimeTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TimeTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ToggleTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ToggleTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.VariableTypeViewState

@Composable
fun ColumnScope.VariableTypeSpecificContent(
    viewState: VariableTypeViewState?,
    onViewStateChanged: (VariableTypeViewState) -> Unit,
) {
    when (viewState) {
        is ColorTypeViewState -> ColorTypeEditor(viewState, onViewStateChanged)
        is ConstantTypeViewState -> ConstantTypeEditor(viewState, onViewStateChanged)
        is DateTypeViewState -> DateTypeEditor(viewState, onViewStateChanged)
        is SelectTypeViewState -> SelectTypeEditor(viewState, onViewStateChanged)
        is SliderTypeViewState -> SliderTypeEditor(viewState, onViewStateChanged)
        is TextTypeViewState -> TextTypeEditor(viewState, onViewStateChanged)
        is TimeTypeViewState -> TimeTypeEditor(viewState, onViewStateChanged)
        is ToggleTypeViewState -> ToggleTypeEditor(viewState, onViewStateChanged)
        null -> Unit
    }
}
