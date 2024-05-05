package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.HelpText
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import java.nio.charset.Charset

@Composable
fun ResponseContent(
    successMessageHint: String,
    responseUiType: String,
    responseSuccessOutput: String,
    responseContentType: ResponseContentType?,
    responseCharset: Charset?,
    availableCharsets: List<Charset>,
    responseFailureOutput: String,
    includeMetaInformation: Boolean,
    successMessage: String,
    responseDisplayActions: List<ResponseDisplayAction>,
    storeResponseIntoFile: Boolean,
    storeDirectory: String?,
    storeFileName: String,
    replaceFileIfExists: Boolean,
    useMonospaceFont: Boolean,
    fontSize: Int?,
    onResponseSuccessOutputChanged: (String) -> Unit,
    onSuccessMessageChanged: (String) -> Unit,
    onResponseFailureOutputChanged: (String) -> Unit,
    onResponseUiTypeChanged: (String) -> Unit,
    onResponseContentTypeChanged: (ResponseContentType?) -> Unit,
    onResponseCharsetChanged: (Charset?) -> Unit,
    onDialogActionChanged: (ResponseDisplayAction?) -> Unit,
    onIncludeMetaInformationChanged: (Boolean) -> Unit,
    onWindowActionsButtonClicked: () -> Unit,
    onStoreResponseIntoFileChanged: (Boolean) -> Unit,
    onReplaceFileIfExistsChanged: (Boolean) -> Unit,
    onStoreFileNameChanged: (String) -> Unit,
    onUseMonospaceFontChanged: (Boolean) -> Unit,
    onFontSizeChanged: (Int?) -> Unit,
) {
    val hasOutput = responseSuccessOutput != ResponseHandling.SUCCESS_OUTPUT_NONE || responseFailureOutput != ResponseHandling.FAILURE_OUTPUT_NONE

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = Spacing.MEDIUM)
    ) {
        SelectionField(
            modifier = Modifier.padding(horizontal = Spacing.MEDIUM),
            title = stringResource(R.string.label_response_handling_type),
            selectedKey = responseUiType,
            items = UI_TYPES.toItems(),
            onItemSelected = onResponseUiTypeChanged,
        )

        AnimatedVisibility(visible = responseUiType == ResponseHandling.UI_TYPE_TOAST) {
            HelpText(
                text = stringResource(R.string.message_response_handling_toast_limitations),
                modifier = Modifier
                    .padding(top = Spacing.TINY)
                    .padding(horizontal = Spacing.MEDIUM),
            )
        }

        Spacer(modifier = Modifier.height(Spacing.MEDIUM))

        Column(
            modifier = Modifier.padding(horizontal = Spacing.MEDIUM),
        ) {
            SelectionField(
                title = stringResource(R.string.label_response_on_success),
                selectedKey = responseSuccessOutput,
                items = SUCCESS_OUTPUT_TYPES.toItems(),
                onItemSelected = onResponseSuccessOutputChanged,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.SMALL))

        SelectionField(
            modifier = Modifier.padding(horizontal = Spacing.MEDIUM),
            title = stringResource(R.string.label_response_on_failure),
            selectedKey = responseFailureOutput,
            items = FAILURE_OUTPUT_TYPES.toItems(),
            onItemSelected = onResponseFailureOutputChanged,
        )

        Spacer(modifier = Modifier.height(Spacing.SMALL))

        SelectionField(
            modifier = Modifier.padding(horizontal = Spacing.MEDIUM),
            title = stringResource(R.string.label_response_charset),
            selectedKey = responseCharset,
            items = listOf(
                null to stringResource(R.string.option_response_charset_auto)
            ) + availableCharsets.map {
                it to it.name()
            },
            onItemSelected = onResponseCharsetChanged,
        )

        AnimatedVisibility(
            visible = hasOutput &&
                responseUiType == ResponseHandling.UI_TYPE_TOAST &&
                responseSuccessOutput == ResponseHandling.SUCCESS_OUTPUT_MESSAGE,
        ) {
            Column {
                Spacer(modifier = Modifier.height(Spacing.MEDIUM))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(Spacing.MEDIUM))

                VariablePlaceholderTextField(
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

        AnimatedVisibility(visible = hasOutput && responseUiType == ResponseHandling.UI_TYPE_DIALOG) {
            Column {
                Spacer(modifier = Modifier.height(Spacing.MEDIUM))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(Spacing.MEDIUM))

                SelectionField(
                    modifier = Modifier
                        .padding(top = Spacing.SMALL)
                        .padding(horizontal = Spacing.MEDIUM),
                    title = stringResource(R.string.label_dialog_action_dropdown),
                    selectedKey = responseDisplayActions.firstOrNull(),
                    items = DIALOG_ACTIONS.toItems(),
                    onItemSelected = onDialogActionChanged,
                )

                SelectionField(
                    modifier = Modifier
                        .padding(vertical = Spacing.SMALL)
                        .padding(horizontal = Spacing.MEDIUM),
                    title = if (responseSuccessOutput == ResponseHandling.SUCCESS_OUTPUT_MESSAGE) {
                        stringResource(R.string.label_response_message_type_selection)
                    } else {
                        stringResource(R.string.label_response_type_selection)
                    },
                    selectedKey = responseContentType,
                    items = RESPONSE_CONTENT_TYPES.toItems(),
                    onItemSelected = onResponseContentTypeChanged,
                )

                AnimatedVisibility(visible = responseSuccessOutput == ResponseHandling.SUCCESS_OUTPUT_MESSAGE) {
                    VariablePlaceholderTextField(
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

                AnimatedVisibility(visible = responseContentType == ResponseContentType.PLAIN_TEXT) {
                    Column {
                        Checkbox(
                            label = stringResource(R.string.label_monospace_response),
                            checked = useMonospaceFont,
                            onCheckedChange = onUseMonospaceFontChanged,
                        )

                        SelectionField(
                            modifier = Modifier
                                .padding(top = Spacing.SMALL)
                                .padding(horizontal = Spacing.MEDIUM),
                            title = stringResource(R.string.label_font_size),
                            selectedKey = fontSize,
                            items = FONT_SIZES.toItems(),
                            onItemSelected = onFontSizeChanged,
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = hasOutput && responseUiType == ResponseHandling.UI_TYPE_WINDOW) {
            Column {
                Spacer(modifier = Modifier.height(Spacing.MEDIUM))

                HorizontalDivider()

                SettingsButton(
                    title = stringResource(R.string.button_select_response_toolbar_buttons),
                    subtitle = pluralStringResource(
                        R.plurals.subtitle_response_toolbar_actions,
                        count = responseDisplayActions.size,
                        responseDisplayActions.size,
                    ),
                    onClick = onWindowActionsButtonClicked,
                )

                SelectionField(
                    modifier = Modifier
                        .padding(
                            horizontal = Spacing.MEDIUM,
                            vertical = Spacing.SMALL,
                        ),
                    title = if (responseSuccessOutput == ResponseHandling.SUCCESS_OUTPUT_MESSAGE) {
                        stringResource(R.string.label_response_message_type_selection)
                    } else {
                        stringResource(R.string.label_response_type_selection)
                    },
                    selectedKey = responseContentType,
                    items = RESPONSE_CONTENT_TYPES.toItems(),
                    onItemSelected = onResponseContentTypeChanged,
                )

                AnimatedVisibility(visible = responseSuccessOutput == ResponseHandling.SUCCESS_OUTPUT_MESSAGE) {
                    VariablePlaceholderTextField(
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

                Checkbox(
                    label = stringResource(R.string.label_include_meta_information),
                    subtitle = stringResource(R.string.subtitle_include_meta_information),
                    checked = includeMetaInformation,
                    onCheckedChange = onIncludeMetaInformationChanged,
                )

                AnimatedVisibility(visible = responseContentType == ResponseContentType.PLAIN_TEXT) {
                    Checkbox(
                        label = stringResource(R.string.label_monospace_response),
                        checked = useMonospaceFont,
                        onCheckedChange = onUseMonospaceFontChanged,
                    )
                }

                AnimatedVisibility(visible = responseContentType != ResponseContentType.HTML) {
                    SelectionField(
                        modifier = Modifier
                            .padding(top = Spacing.SMALL)
                            .padding(horizontal = Spacing.MEDIUM),
                        title = stringResource(R.string.label_font_size),
                        selectedKey = fontSize,
                        items = FONT_SIZES.toItems(),
                        onItemSelected = onFontSizeChanged,
                    )
                }
            }
        }

        Column {
            Spacer(modifier = Modifier.height(Spacing.MEDIUM))

            HorizontalDivider()

            Checkbox(
                label = stringResource(R.string.label_store_response_into_file),
                subtitle = storeDirectory?.let {
                    stringResource(R.string.subtitle_store_response_into_file_directory, it)
                },
                checked = storeResponseIntoFile,
                onCheckedChange = onStoreResponseIntoFileChanged,
            )

            AnimatedVisibility(visible = storeResponseIntoFile) {
                Column(
                    modifier = Modifier.padding(bottom = Spacing.MEDIUM)
                ) {
                    Checkbox(
                        label = stringResource(R.string.label_store_response_replace_file),
                        checked = replaceFileIfExists,
                        onCheckedChange = onReplaceFileIfExistsChanged,
                    )

                    VariablePlaceholderTextField(
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
    ResponseHandling.UI_TYPE_TOAST to R.string.option_response_handling_type_toast,
    ResponseHandling.UI_TYPE_DIALOG to R.string.option_response_handling_type_dialog,
    ResponseHandling.UI_TYPE_WINDOW to R.string.option_response_handling_type_window,
)

private val SUCCESS_OUTPUT_TYPES = listOf(
    ResponseHandling.SUCCESS_OUTPUT_RESPONSE to R.string.option_response_handling_success_output_response,
    ResponseHandling.SUCCESS_OUTPUT_MESSAGE to R.string.option_response_handling_success_output_message,
    ResponseHandling.SUCCESS_OUTPUT_NONE to R.string.option_response_handling_success_output_none,
)

private val FAILURE_OUTPUT_TYPES = listOf(
    ResponseHandling.FAILURE_OUTPUT_DETAILED to R.string.option_response_handling_failure_output_detailed,
    ResponseHandling.FAILURE_OUTPUT_SIMPLE to R.string.option_response_handling_failure_output_simple,
    ResponseHandling.FAILURE_OUTPUT_NONE to R.string.option_response_handling_failure_output_none,
)

private val DIALOG_ACTIONS = listOf(
    null to R.string.label_dialog_action_none,
    ResponseDisplayAction.RERUN to R.string.action_rerun_shortcut,
    ResponseDisplayAction.SHARE to R.string.share_button,
    ResponseDisplayAction.COPY to R.string.action_copy_response,
)

private val RESPONSE_CONTENT_TYPES = listOf(
    null to R.string.option_response_content_type_auto,
    ResponseContentType.PLAIN_TEXT to R.string.option_response_content_type_plain_text,
    ResponseContentType.JSON to R.string.option_response_content_type_json,
    ResponseContentType.XML to R.string.option_response_content_type_xml,
    ResponseContentType.HTML to R.string.option_response_content_type_html,
)

private val FONT_SIZES = listOf(
    8 to R.string.font_size_tiny,
    11 to R.string.font_size_very_small,
    13 to R.string.font_size_small,
    null to R.string.font_size_regular,
    19 to R.string.font_size_large,
    23 to R.string.font_size_very_large,
    27 to R.string.font_size_huge,
)

@Composable
private fun <T> List<Pair<T, Int>>.toItems() =
    map { (value, label) -> value to stringResource(label) }
