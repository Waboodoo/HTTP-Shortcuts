package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.HelpText
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType

@Composable
fun ResponseContent(
    savedStateHandle: SavedStateHandle,
    successMessageHint: String,
    responseUiType: ResponseUiType,
    responseSuccessOutput: ResponseSuccessOutput,
    responseFailureOutput: ResponseFailureOutput,
    successMessage: String,
    responseCharset: String?,
    availableCharsets: List<String>,
    storeResponseIntoFile: Boolean,
    storeDirectoryName: String?,
    storeFileName: String,
    replaceFileIfExists: Boolean,
    onResponseSuccessOutputChanged: (ResponseSuccessOutput) -> Unit,
    onSuccessMessageChanged: (String) -> Unit,
    onResponseFailureOutputChanged: (ResponseFailureOutput) -> Unit,
    onResponseUiTypeChanged: (ResponseUiType) -> Unit,
    onDisplaySettingsClicked: () -> Unit,
    onResponseCharsetChanged: (String?) -> Unit,
    onStoreResponseIntoFileChanged: (Boolean) -> Unit,
    onReplaceFileIfExistsChanged: (Boolean) -> Unit,
    onStoreFileNameChanged: (String) -> Unit,
) {
    val hasOutput = responseSuccessOutput != ResponseSuccessOutput.NONE || responseFailureOutput != ResponseFailureOutput.NONE

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = Spacing.MEDIUM),
    ) {
        SelectionField(
            modifier = Modifier.padding(horizontal = Spacing.MEDIUM),
            title = stringResource(R.string.label_response_on_success),
            selectedKey = responseSuccessOutput,
            items = SUCCESS_OUTPUT_TYPES.toItems(),
            onItemSelected = onResponseSuccessOutputChanged,
        )

        AnimatedVisibility(
            visible = responseSuccessOutput == ResponseSuccessOutput.MESSAGE,
        ) {
            Column {
                VariablePlaceholderTextField(
                    savedStateHandle = savedStateHandle,
                    modifier = Modifier.padding(
                        horizontal = Spacing.MEDIUM,
                        vertical = Spacing.SMALL,
                    ),
                    key = "success-message-input",
                    label = {
                        Text(stringResource(R.string.label_response_handling_success_message))
                    },
                    placeholder = {
                        Text(successMessageHint)
                    },
                    value = successMessage,
                    onValueChange = onSuccessMessageChanged,
                    maxLines = 10,
                )
            }
        }

        VerticalSpacer(Spacing.SMALL)

        SelectionField(
            modifier = Modifier.padding(horizontal = Spacing.MEDIUM),
            title = stringResource(R.string.label_response_on_failure),
            selectedKey = responseFailureOutput,
            items = FAILURE_OUTPUT_TYPES.toItems(),
            onItemSelected = onResponseFailureOutputChanged,
        )

        VerticalSpacer(Spacing.MEDIUM)

        HorizontalDivider()

        VerticalSpacer(Spacing.MEDIUM)

        SelectionField(
            modifier = Modifier.padding(horizontal = Spacing.MEDIUM),
            title = stringResource(R.string.label_response_handling_type),
            selectedKey = responseUiType,
            items = UI_TYPES.toItems(),
            enabled = hasOutput,
            onItemSelected = onResponseUiTypeChanged,
        )

        AnimatedVisibility(visible = responseUiType == ResponseUiType.TOAST) {
            HelpText(
                text = stringResource(R.string.message_response_handling_toast_limitations),
                modifier = Modifier
                    .padding(top = Spacing.TINY)
                    .padding(horizontal = Spacing.MEDIUM),
            )
        }

        SettingsButton(
            enabled = hasOutput && (responseUiType == ResponseUiType.DIALOG || responseUiType == ResponseUiType.WINDOW),
            title = stringResource(R.string.button_display_settings),
            subtitle = if (hasOutput && responseSuccessOutput == ResponseSuccessOutput.NONE) {
                stringResource(R.string.subtitle_display_settings_for_error)
            } else if (responseSuccessOutput == ResponseSuccessOutput.MESSAGE) {
                stringResource(R.string.subtitle_display_settings_for_message)
            } else {
                stringResource(R.string.subtitle_display_settings_for_response)
            },
            onClick = onDisplaySettingsClicked,
        )

        HorizontalDivider()

        VerticalSpacer(Spacing.MEDIUM)

        SelectionField(
            modifier = Modifier.padding(horizontal = Spacing.MEDIUM),
            title = stringResource(R.string.label_response_charset),
            selectedKey = responseCharset,
            items = listOf(
                null to stringResource(R.string.option_response_charset_auto),
            ) + availableCharsets.map { it to it },
            onItemSelected = onResponseCharsetChanged,
        )

        VerticalSpacer(Spacing.MEDIUM)

        HorizontalDivider()

        Checkbox(
            label = stringResource(R.string.label_store_response_into_file),
            subtitle = storeDirectoryName?.let {
                stringResource(R.string.subtitle_store_response_into_file_directory, it)
            },
            checked = storeResponseIntoFile,
            onCheckedChange = onStoreResponseIntoFileChanged,
        )

        AnimatedVisibility(visible = storeResponseIntoFile) {
            Column(
                modifier = Modifier.padding(bottom = Spacing.MEDIUM),
            ) {
                Checkbox(
                    label = stringResource(R.string.label_store_response_replace_file),
                    checked = replaceFileIfExists,
                    onCheckedChange = onReplaceFileIfExistsChanged,
                )

                VariablePlaceholderTextField(
                    savedStateHandle = savedStateHandle,
                    modifier = Modifier.padding(horizontal = Spacing.MEDIUM),
                    key = "store-file-name",
                    label = {
                        Text(stringResource(R.string.label_store_response_file_name))
                    },
                    singleLine = true,
                    value = storeFileName,
                    onValueChange = onStoreFileNameChanged,
                )
            }
        }

        HorizontalDivider()

        HelpText(
            text = stringResource(R.string.message_response_handling_scripting_hint, stringResource(R.string.label_scripting)),
            modifier = Modifier
                .padding(top = Spacing.MEDIUM)
                .padding(horizontal = Spacing.MEDIUM),
        )
    }
}

private val UI_TYPES = listOf(
    ResponseUiType.TOAST to R.string.option_response_handling_type_toast,
    ResponseUiType.NOTIFICATION to R.string.option_response_handling_type_notification,
    ResponseUiType.DIALOG to R.string.option_response_handling_type_dialog,
    ResponseUiType.WINDOW to R.string.option_response_handling_type_window,
)

private val SUCCESS_OUTPUT_TYPES = listOf(
    ResponseSuccessOutput.RESPONSE to R.string.option_response_handling_success_output_response,
    ResponseSuccessOutput.MESSAGE to R.string.option_response_handling_success_output_message,
    ResponseSuccessOutput.NONE to R.string.option_response_handling_success_output_none,
)

private val FAILURE_OUTPUT_TYPES = listOf(
    ResponseFailureOutput.DETAILED to R.string.option_response_handling_failure_output_detailed,
    ResponseFailureOutput.SIMPLE to R.string.option_response_handling_failure_output_simple,
    ResponseFailureOutput.NONE to R.string.option_response_handling_failure_output_none,
)

@Composable
private fun <T> List<Pair<T, Int>>.toItems() =
    map { (value, label) -> value to stringResource(label) }
