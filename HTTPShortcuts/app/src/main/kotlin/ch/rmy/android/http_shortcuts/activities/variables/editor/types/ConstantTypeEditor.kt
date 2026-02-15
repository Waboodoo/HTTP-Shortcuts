package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ConstantTypeViewModel.Companion.SECRET_VALUE
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField
import ch.rmy.android.http_shortcuts.components.clickOnlyInteractionSource
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConstantTypeEditor(
    savedStateHandle: SavedStateHandle,
    viewState: ConstantTypeViewState,
    onViewStateChanged: (ConstantTypeViewState) -> Unit,
) {
    val focusRequester = remember {
        FocusRequester()
    }
    val coroutineScope = rememberCoroutineScope()
    if (viewState.isSecret && viewState.value == SECRET_VALUE) {
        val label = stringResource(R.string.placeholder_value)
        val onClick = remember {
            {
                onViewStateChanged(viewState.copy(value = ""))
                coroutineScope.launch {
                    delay(200.milliseconds)
                    focusRequester.requestFocus()
                }
            }
        }
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.MEDIUM)
                .clearAndSetSemantics {
                    contentDescription = label
                    role = Role.Button
                    onClick {
                        onClick()
                        true
                    }
                },
            label = {
                Text(label)
            },
            value = SECRET_VALUE,
            onValueChange = {},
            visualTransformation = PasswordVisualTransformation(),
            interactionSource = clickOnlyInteractionSource {
                onClick()
            },
            readOnly = true,
        )
    } else {
        VariablePlaceholderTextField(
            savedStateHandle = savedStateHandle,
            modifier = Modifier
                .padding(horizontal = Spacing.MEDIUM)
                .focusRequester(focusRequester),
            allowOpeningVariableEditor = false,
            label = {
                Text(stringResource(R.string.placeholder_value))
            },
            value = viewState.value,
            maxLength = 40_000,
            onValueChange = { value ->
                if (value != SECRET_VALUE) {
                    onViewStateChanged(viewState.copy(value = value))
                }
            },
            maxLines = 12,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (viewState.isSecret) KeyboardType.Password else KeyboardType.Unspecified,
            ),
        )
    }

    Checkbox(
        label = stringResource(R.string.label_secret_variable_value),
        subtitle = stringResource(R.string.subtitle_secret_variable_value),
        checked = viewState.isSecret,
        onCheckedChange = { value ->
            onViewStateChanged(
                viewState.copy(
                    isSecret = value,
                    value = if (!value && viewState.value == SECRET_VALUE) "" else viewState.value,
                ),
            )
        },
    )
}
