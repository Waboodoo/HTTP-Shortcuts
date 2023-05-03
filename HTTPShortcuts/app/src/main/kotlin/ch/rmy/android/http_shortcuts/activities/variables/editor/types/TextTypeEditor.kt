package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsCheckbox

@Composable
fun TextTypeEditor(
    viewState: TextTypeViewState,
    onViewStateChanged: (TextTypeViewState) -> Unit,
) {
    Column {
        if (viewState.isMultilineCheckboxVisible) {
            SettingsCheckbox(
                title = {
                    Text(stringResource(R.string.label_multiline))
                },
                state = rememberBooleanSettingState(viewState.isMultiline),
                onCheckedChange = {
                    onViewStateChanged(viewState.copy(isMultiline = it))
                },
            )
        }

        SettingsCheckbox(
            title = {
                Text(stringResource(R.string.label_remember_value))
            },
            state = rememberBooleanSettingState(viewState.rememberValue),
            onCheckedChange = {
                onViewStateChanged(viewState.copy(rememberValue = it))
            },
        )
    }
}
