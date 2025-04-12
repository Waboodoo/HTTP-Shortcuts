package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField

@Composable
fun IncrementTypeEditor(
    savedStateHandle: SavedStateHandle,
    viewState: IncrementTypeViewState,
    onViewStateChanged: (IncrementTypeViewState) -> Unit,
) {
    VariablePlaceholderTextField(
        savedStateHandle = savedStateHandle,
        modifier = Modifier
            .padding(horizontal = Spacing.MEDIUM),
        key = "increment-value-field",
        allowOpeningVariableEditor = false,
        label = {
            Text(stringResource(R.string.placeholder_value))
        },
        value = viewState.value,
        maxLength = 12,
        onValueChange = { value ->
            onViewStateChanged(viewState.copy(value = value))
        },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Number,
        ),
        singleLine = true,
    )
}
