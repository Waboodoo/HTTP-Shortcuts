package ch.rmy.android.http_shortcuts.activities.variables.editor

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ColorTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ColorTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ConstantTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ConstantTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.DateTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.DateTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.IncrementTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.IncrementTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.SelectTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.SelectTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.SliderTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.SliderTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TextTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TextTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TimeTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TimeTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TimestampTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TimestampTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ToggleTypeEditor
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ToggleTypeViewState
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.VariableTypeViewState

@Composable
fun VariableTypeSpecificContent(
    savedStateHandle: SavedStateHandle,
    viewState: VariableTypeViewState?,
    onViewStateChanged: (VariableTypeViewState) -> Unit,
) {
    when (viewState) {
        is ColorTypeViewState -> ColorTypeEditor(viewState, onViewStateChanged)
        is ConstantTypeViewState -> ConstantTypeEditor(savedStateHandle, viewState, onViewStateChanged)
        is DateTypeViewState -> DateTypeEditor(viewState, onViewStateChanged)
        is SelectTypeViewState -> SelectTypeEditor(savedStateHandle, viewState, onViewStateChanged)
        is SliderTypeViewState -> SliderTypeEditor(viewState, onViewStateChanged)
        is TextTypeViewState -> TextTypeEditor(viewState, onViewStateChanged)
        is TimeTypeViewState -> TimeTypeEditor(viewState, onViewStateChanged)
        is TimestampTypeViewState -> TimestampTypeEditor(viewState, onViewStateChanged)
        is ToggleTypeViewState -> ToggleTypeEditor(savedStateHandle, viewState, onViewStateChanged)
        is IncrementTypeViewState -> IncrementTypeEditor(savedStateHandle, viewState, onViewStateChanged)
        null -> Unit
    }
}
