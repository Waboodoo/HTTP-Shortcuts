package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.Spacing

@Composable
fun SliderTypeEditor(
    viewState: SliderTypeViewState,
    onViewStateChanged: (SliderTypeViewState) -> Unit,
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.MEDIUM)
            .padding(top = Spacing.SMALL),
        label = {
            Text(stringResource(R.string.label_variable_slider_min))
        },
        value = viewState.minValueText,
        onValueChange = {
            onViewStateChanged(
                viewState.copy(
                    minValueText = it.take(10),
                ),
            )
        },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Decimal,
        ),
        singleLine = true,
    )

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.MEDIUM),
        label = {
            Text(stringResource(R.string.label_variable_slider_max))
        },
        value = viewState.maxValueText,
        onValueChange = {
            onViewStateChanged(
                viewState.copy(
                    maxValueText = it.take(10),
                ),
            )
        },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Decimal,
        ),
        isError = viewState.isMaxValueInvalid,
        supportingText = if (viewState.isMaxValueInvalid) {
            {
                Text(stringResource(R.string.error_slider_max_not_greater_than_min))
            }
        } else {
            null
        },
        singleLine = true,
    )

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.MEDIUM),
        label = {
            Text(stringResource(R.string.label_variable_slider_step))
        },
        value = viewState.stepSizeText,
        onValueChange = {
            onViewStateChanged(
                viewState.copy(
                    stepSizeText = it.take(10),
                ),
            )
        },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Decimal,
        ),
        isError = viewState.isStepSizeInvalid,
        supportingText = if (viewState.isStepSizeInvalid) {
            {
                Text(stringResource(R.string.error_slider_step_size_must_be_positive))
            }
        } else {
            null
        },
        singleLine = true,
    )

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.MEDIUM)
            .padding(top = Spacing.SMALL),
        label = {
            Text(stringResource(R.string.label_variable_slider_prefix))
        },
        value = viewState.prefix,
        onValueChange = {
            onViewStateChanged(
                viewState.copy(
                    prefix = it.take(100),
                ),
            )
        },
        singleLine = true,
    )

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.MEDIUM),
        label = {
            Text(stringResource(R.string.label_variable_slider_suffix))
        },
        value = viewState.suffix,
        onValueChange = {
            onViewStateChanged(
                viewState.copy(
                    suffix = it.take(100),
                ),
            )
        },
        singleLine = true,
    )

    Checkbox(
        label = stringResource(R.string.label_remember_value),
        checked = viewState.rememberValue,
        onCheckedChange = {
            onViewStateChanged(viewState.copy(rememberValue = it))
        },
    )
}
