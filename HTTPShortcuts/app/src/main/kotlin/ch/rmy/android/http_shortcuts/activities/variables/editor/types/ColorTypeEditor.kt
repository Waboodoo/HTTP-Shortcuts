package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Checkbox

@Composable
fun ColorTypeEditor(
    viewState: ColorTypeViewState,
    onViewStateChanged: (ColorTypeViewState) -> Unit,
) {
    Checkbox(
        label = stringResource(R.string.label_remember_value),
        checked = viewState.rememberValue,
        onCheckedChange = {
            onViewStateChanged(viewState.copy(rememberValue = it))
        },
    )
}
