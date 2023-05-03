package ch.rmy.android.http_shortcuts.activities.variables.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.editor.models.ShareSupport
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.extensions.localize
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsGroup

@Composable
fun ScreenScope.VariableEditorContent(
    variableKey: String,
    dialogTitle: String,
    dialogMessage: String,
    urlEncodeChecked: Boolean,
    jsonEncodeChecked: Boolean,
    allowShareChecked: Boolean,
    shareSupport: ShareSupport,
    variableKeyInputError: Localizable?,
    dialogTitleVisible: Boolean,
    dialogMessageVisible: Boolean,
    shareSupportVisible: Boolean,
    onVariableKeyChanged: (String) -> Unit,
    onDialogTitleChanged: (String) -> Unit,
    onDialogMessageChanged: (String) -> Unit,
    onUrlEncodeChanged: (Boolean) -> Unit,
    onJsonEncodeChanged: (Boolean) -> Unit,
    onAllowShareChanged: (Boolean) -> Unit,
    onShareSupportChanged: (ShareSupport) -> Unit,
    typeSpecificContent: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        SettingsGroup(
            title = { Text(stringResource(R.string.section_basic_variable_settings)) },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                VariableKey(
                    key = variableKey,
                    error = variableKeyInputError?.localize(),
                    onKeyChanged = onVariableKeyChanged,
                )

                if (dialogTitleVisible) {
                    DialogTitle(
                        title = dialogTitle,
                        onTitleChanged = onDialogTitleChanged,
                    )
                }
                if (dialogMessageVisible) {
                    DialogMessage(
                        message = dialogMessage,
                        onMessageChanged = onDialogMessageChanged,
                    )
                }
            }
        }

        typeSpecificContent()

        SettingsGroup(
            title = { Text(stringResource(R.string.section_advanced_settings)) },
        ) {
            Checkbox(
                label = stringResource(R.string.label_url_encode),
                helpText = stringResource(R.string.message_url_encode_instructions),
                checked = urlEncodeChecked,
                onCheckedChange = onUrlEncodeChanged,
            )

            Checkbox(
                label = stringResource(R.string.label_json_encode),
                helpText = stringResource(R.string.message_json_encode_instructions),
                checked = jsonEncodeChecked,
                onCheckedChange = onJsonEncodeChanged,
            )

            Column {
                Checkbox(
                    label = stringResource(R.string.label_allow_share_into),
                    helpText = stringResource(R.string.message_allow_share_instructions),
                    checked = allowShareChecked,
                    onCheckedChange = onAllowShareChanged,
                )
                AnimatedVisibility(visible = shareSupportVisible) {
                    ShareSupportSelection(
                        shareSupport = shareSupport,
                        onShareSupportChanged = onShareSupportChanged,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenScope.VariableKey(
    key: String,
    error: String?,
    onKeyChanged: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    EventHandler {
        when (it) {
            is VariableEditorEvent.FocusVariableKeyInput -> consume {
                focusRequester.requestFocus()
            }
            else -> false
        }
    }

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.MEDIUM)
            .focusRequester(focusRequester),
        label = {
            Text(stringResource(R.string.label_variable_name))
        },
        value = key,
        onValueChange = {
            onKeyChanged(it.take(30))
        },
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            capitalization = KeyboardCapitalization.None,
        ),
        singleLine = true,
        isError = error != null,
        supportingText = error?.let {
            {
                Text(it)
            }
        }
    )
}

@Composable
private fun DialogTitle(title: String, onTitleChanged: (String) -> Unit) {
    TextField(
        modifier = Modifier
            .padding(horizontal = Spacing.MEDIUM)
            .fillMaxWidth(),
        label = {
            Text(stringResource(R.string.label_variable_title))
        },
        value = title,
        onValueChange = {
            onTitleChanged(it.take(20))
        },
        singleLine = true,
    )
}

@Composable
private fun DialogMessage(message: String, onMessageChanged: (String) -> Unit) {
    TextField(
        modifier = Modifier
            .padding(horizontal = Spacing.MEDIUM)
            .fillMaxWidth(),
        label = {
            Text(stringResource(R.string.label_variable_text))
        },
        value = message,
        onValueChange = {
            onMessageChanged(it.take(200))
        },
        maxLines = 3,
    )
}

@Composable
private fun Checkbox(
    label: String,
    helpText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsCheckbox(
        title = {
            Text(label)
        },
        subtitle = {
            Text(helpText)
        },
        state = rememberBooleanSettingState(checked),
        onCheckedChange = onCheckedChange,
    )
}

@Composable
private fun ShareSupportSelection(
    shareSupport: ShareSupport,
    onShareSupportChanged: (ShareSupport) -> Unit,
) {
    SelectionField(
        modifier = Modifier.padding(horizontal = Spacing.MEDIUM),
        title = stringResource(R.string.label_share_support),
        selectedKey = shareSupport,
        items = listOf(
            ShareSupport.TEXT to stringResource(R.string.label_share_support_option_text),
            ShareSupport.TITLE to stringResource(R.string.label_share_support_option_title),
            ShareSupport.TITLE_AND_TEXT to stringResource(R.string.label_share_support_option_title_and_text),
        ),
        onItemSelected = onShareSupportChanged,
    )
}
