package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField

@Composable
fun ConstantTypeEditor(
    viewState: ConstantTypeViewState,
    onViewStateChanged: (ConstantTypeViewState) -> Unit,
) {
    VariablePlaceholderTextField(
        modifier = Modifier
            .padding(horizontal = Spacing.MEDIUM),
        key = "constant-value-field",
        label = {
            Text(stringResource(R.string.placeholder_value))
        },
        value = viewState.value,
        onValueChange = {
            onViewStateChanged(viewState.copy(value = it.take(30_000)))
        },
        maxLines = 12,
    )
}
