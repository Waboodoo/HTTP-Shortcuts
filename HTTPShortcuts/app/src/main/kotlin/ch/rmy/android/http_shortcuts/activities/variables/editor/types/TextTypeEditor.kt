package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Checkbox

@Composable
fun TextTypeEditor(
    viewState: TextTypeViewState,
    onViewStateChanged: (TextTypeViewState) -> Unit,
) {
    Column {
        if (viewState.isMultilineCheckboxVisible) {
            Checkbox(
                label = stringResource(R.string.label_multiline),
                checked = viewState.isMultiline,
                onCheckedChange = {
                    onViewStateChanged(viewState.copy(isMultiline = it))
                },
            )
        }

        Checkbox(
            label = stringResource(R.string.label_remember_value),
            checked = viewState.rememberValue,
            onCheckedChange = {
                onViewStateChanged(viewState.copy(rememberValue = it))
            },
        )
    }
}
