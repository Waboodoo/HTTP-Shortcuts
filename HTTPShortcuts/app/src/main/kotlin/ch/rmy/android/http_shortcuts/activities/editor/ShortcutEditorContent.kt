package ch.rmy.android.http_shortcuts.activities.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.ShortcutIcon
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderText
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Composable
fun ShortcutEditorContent(
    shortcutName: String,
    shortcutIcon: ShortcutIcon,
    shortcutDescription: String,
    shortcutExecutionType: ShortcutExecutionType,
    basicSettingsSubtitle: String,
    headersSubtitle: String,
    requestBodySubtitle: String,
    authenticationSettingsSubtitle: String,
    scriptingSubtitle: String,
    triggerShortcutsSubtitle: String,
    requestBodyButtonEnabled: Boolean,
    iconLoading: Boolean,
    onShortcutNameChanged: (String) -> Unit,
    onShortcutDescriptionChanged: (String) -> Unit,
    onShortcutIconClicked: () -> Unit,
    onBasicRequestButtonClicked: () -> Unit,
    onHeadersButtonClicked: () -> Unit,
    onRequestBodyButtonClicked: () -> Unit,
    onAuthenticationButtonClicked: () -> Unit,
    onResponseHandlingButtonClicked: () -> Unit,
    onScriptingButtonClicked: () -> Unit,
    onTriggerShortcutsButtonClicked: () -> Unit,
    onExecutionSettingsButtonClicked: () -> Unit,
    onAdvancedSettingsButtonClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing.MEDIUM),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.MEDIUM)
                .padding(top = Spacing.TINY, bottom = Spacing.MEDIUM),
            verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShortcutNameField(
                    modifier = Modifier.weight(1f),
                    name = shortcutName,
                    onNameChanged = onShortcutNameChanged,
                )

                Box {
                    ShortcutIcon(
                        shortcutIcon = shortcutIcon,
                        contentDescription = stringResource(R.string.icon_description),
                        modifier = Modifier
                            .alpha(if (iconLoading) 0.7f else 1f)
                            .clickable(
                                enabled = !iconLoading,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = false),
                                onClick = onShortcutIconClicked,
                            ),
                    )
                    if (iconLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.matchParentSize(),
                        )
                    }
                }
            }

            ShortcutDescriptionField(
                modifier = Modifier.fillMaxWidth(),
                description = shortcutDescription,
                onDescriptionChanged = onShortcutDescriptionChanged,
            )
        }

        HorizontalDivider(modifier = Modifier.padding(bottom = Spacing.SMALL))

        if (shortcutExecutionType.usesUrl) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.section_basic_request)) },
                supportingContent = { VariablePlaceholderText(basicSettingsSubtitle) },
                modifier = Modifier
                    .clickable(onClick = onBasicRequestButtonClicked)
            )
        }

        if (shortcutExecutionType.usesRequestOptions) {
            SettingsButton(
                title = stringResource(R.string.section_request_headers),
                subtitle = headersSubtitle,
                onClick = onHeadersButtonClicked,
            )

            SettingsButton(
                title = stringResource(R.string.section_request_body),
                subtitle = requestBodySubtitle,
                enabled = requestBodyButtonEnabled,
                onClick = onRequestBodyButtonClicked,
            )

            SettingsButton(
                title = stringResource(R.string.section_authentication),
                subtitle = authenticationSettingsSubtitle,
                onClick = onAuthenticationButtonClicked,
            )
        }

        if (shortcutExecutionType.usesResponse) {
            SettingsButton(
                title = stringResource(R.string.label_response_handling),
                subtitle = stringResource(R.string.label_response_handling_subtitle),
                onClick = onResponseHandlingButtonClicked,
            )
        }

        if (shortcutExecutionType.usesScriptingEditor) {
            SettingsButton(
                title = stringResource(R.string.label_scripting),
                subtitle = scriptingSubtitle,
                onClick = onScriptingButtonClicked,
            )
        }

        if (shortcutExecutionType == ShortcutExecutionType.TRIGGER) {
            SettingsButton(
                title = stringResource(R.string.label_trigger_shortcuts),
                subtitle = triggerShortcutsSubtitle,
                onClick = onTriggerShortcutsButtonClicked,
            )
        }

        SettingsButton(
            title = stringResource(R.string.label_execution_settings),
            subtitle = stringResource(R.string.label_execution_settings_subtitle),
            onClick = onExecutionSettingsButtonClicked,
        )

        if (shortcutExecutionType.usesRequestOptions) {
            SettingsButton(
                title = stringResource(R.string.label_advanced_technical_settings),
                subtitle = stringResource(R.string.label_advanced_technical_settings_subtitle),
                onClick = onAdvancedSettingsButtonClicked,
            )
        }
    }
}

@Composable
private fun ShortcutNameField(
    modifier: Modifier,
    name: String,
    onNameChanged: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    EventHandler {
        when (it) {
            is ShortcutEditorEvent.FocusNameInputField -> consume {
                focusRequester.requestFocus()
                keyboard?.show()
            }
            else -> false
        }
    }

    TextField(
        modifier = Modifier
            .focusRequester(focusRequester)
            .then(modifier),
        label = {
            Text(stringResource(R.string.label_name))
        },
        value = name,
        onValueChange = {
            onNameChanged(it.take(Shortcut.NAME_MAX_LENGTH))
        },
        singleLine = true,
    )
}

@Composable
private fun ShortcutDescriptionField(
    modifier: Modifier,
    description: String,
    onDescriptionChanged: (String) -> Unit,
) {
    TextField(
        modifier = modifier,
        label = {
            Text(stringResource(R.string.label_description))
        },
        value = description,
        onValueChange = {
            onDescriptionChanged(it.take(200))
        },
    )
}
