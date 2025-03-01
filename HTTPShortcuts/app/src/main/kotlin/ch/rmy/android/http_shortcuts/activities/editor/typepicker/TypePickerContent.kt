package ch.rmy.android.http_shortcuts.activities.editor.typepicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.ScreenInstructionsHeaders
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType

@Composable
fun TypePickerContent(
    onShortcutTypeSelected: (ShortcutExecutionType) -> Unit,
    onCurlImportSelected: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        ScreenInstructionsHeaders(stringResource(R.string.instructions_create_new_shortcut_options_dialog))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = Spacing.MEDIUM + Spacing.SMALL),
        ) {
            SectionTitle(stringResource(R.string.button_create_new))

            Option(
                label = stringResource(R.string.button_create_from_scratch),
                icon = Icons.Filled.AddCircleOutline,
                onClick = {
                    onShortcutTypeSelected(ShortcutExecutionType.HTTP)
                },
            )

            VerticalSpacer(Spacing.SMALL)

            Option(
                label = stringResource(R.string.button_curl_import),
                icon = Icons.Filled.Terminal,
                onClick = onCurlImportSelected,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.MEDIUM + Spacing.SMALL))

            SectionTitle(stringResource(R.string.section_title_advanced_shortcut_types))

            Option(
                label = stringResource(R.string.button_create_trigger_shortcut),
                description = stringResource(R.string.button_description_create_trigger_shortcut),
                icon = Icons.AutoMirrored.Filled.ListAlt,
                onClick = {
                    onShortcutTypeSelected(ShortcutExecutionType.TRIGGER)
                },
            )

            VerticalSpacer(Spacing.SMALL)

            Option(
                label = stringResource(R.string.button_create_browser_shortcut),
                description = stringResource(R.string.button_description_create_browser_shortcut),
                icon = Icons.Filled.OpenInBrowser,
                onClick = {
                    onShortcutTypeSelected(ShortcutExecutionType.BROWSER)
                },
            )

            VerticalSpacer(Spacing.SMALL)

            Option(
                label = stringResource(R.string.button_create_mqtt_shortcut),
                description = stringResource(R.string.button_description_create_mqtt_shortcut),
                icon = Icons.Filled.MailOutline,
                onClick = {
                    onShortcutTypeSelected(ShortcutExecutionType.MQTT)
                },
            )

            VerticalSpacer(Spacing.SMALL)

            Option(
                label = stringResource(R.string.button_create_wol_shortcut),
                description = stringResource(R.string.button_description_create_wol_shortcut),
                icon = Icons.Filled.PowerSettingsNew,
                onClick = {
                    onShortcutTypeSelected(ShortcutExecutionType.WAKE_ON_LAN)
                },
            )

            VerticalSpacer(Spacing.SMALL)

            Option(
                label = stringResource(R.string.button_create_scripting_shortcut),
                description = stringResource(R.string.button_description_create_scripting_shortcut),
                icon = Icons.Filled.Code,
                onClick = {
                    onShortcutTypeSelected(ShortcutExecutionType.SCRIPTING)
                },
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    ProvideTextStyle(value = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary)) {
        Text(
            modifier = Modifier
                .semantics {
                    heading()
                }
                .padding(horizontal = Spacing.MEDIUM)
                .padding(bottom = Spacing.MEDIUM),
            text = text,
        )
    }
}

@Composable
private fun Option(
    label: String,
    description: String? = null,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.MEDIUM)
            .padding(vertical = Spacing.SMALL + Spacing.TINY),
        horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
        )

        Column {
            Text(
                label,
                fontSize = FontSize.BIG,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
            if (description != null) {
                Text(
                    description,
                    fontSize = FontSize.SMALL,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
private fun TypePickerContent_Preview() {
    TypePickerContent(
        onShortcutTypeSelected = {},
        onCurlImportSelected = {},
    )
}
