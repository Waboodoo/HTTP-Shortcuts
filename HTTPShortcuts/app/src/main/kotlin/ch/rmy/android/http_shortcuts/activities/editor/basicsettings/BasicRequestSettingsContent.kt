package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.models.InstalledBrowser
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.data.enums.HttpMethod

@Composable
fun HttpSettingsContent(
    savedStateHandle: SavedStateHandle,
    method: HttpMethod,
    url: String,
    onMethodChanged: (HttpMethod) -> Unit,
    onUrlChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        MethodSelection(method, onMethodChanged)

        UrlField(savedStateHandle, url, onUrlChanged)
    }
}

@Composable
fun BrowserSettingsContent(
    savedStateHandle: SavedStateHandle,
    url: String,
    targetBrowser: TargetBrowser,
    browserPackageNameOptions: List<InstalledBrowser>,
    onUrlChanged: (String) -> Unit,
    onTargetBrowserChanged: (TargetBrowser) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        UrlField(savedStateHandle, url, onUrlChanged)

        TargetBrowserSelection(
            targetBrowser = targetBrowser,
            browserPackageNameOptions = browserPackageNameOptions,
            onTargetBrowserChanged = onTargetBrowserChanged,
        )
    }
}

@Composable
fun MqttSettingsContent(
    savedStateHandle: SavedStateHandle,
    url: String,
    onUrlChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        UrlField(
            savedStateHandle = savedStateHandle,
            url = url,
            onUrlChanged = onUrlChanged,
        )
    }
}

@Composable
fun WakOnLanSettingsContent(
    savedStateHandle: SavedStateHandle,
    macAddress: String,
    port: String,
    broadcastAddress: String,
    onMacAddressChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onBroadcastAddressChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        MacAddressField(
            savedStateHandle = savedStateHandle,
            macAddress = macAddress,
            onMacAddressChanged = onMacAddressChanged,
        )

        PortField(
            port = port,
            onPortChanged = onPortChanged,
        )

        BroadcastAddressField(
            broadcastAddress = broadcastAddress,
            onBroadcastAddressChanged = onBroadcastAddressChanged,
        )
    }
}

@Composable
private fun MethodSelection(
    method: HttpMethod,
    onMethodSelected: (HttpMethod) -> Unit,
) {
    SelectionField(
        title = stringResource(R.string.label_method),
        selectedKey = method,
        items = HttpMethod.entries.map { it to it.method },
        onItemSelected = onMethodSelected,
    )
}

@Composable
private fun UrlField(
    savedStateHandle: SavedStateHandle,
    url: String,
    onUrlChanged: (String) -> Unit,
) {
    VariablePlaceholderTextField(
        savedStateHandle = savedStateHandle,
        key = "url-input",
        modifier = Modifier
            .fillMaxWidth(),
        label = {
            Text(stringResource(R.string.label_url))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Uri,
        ),
        value = url,
        onValueChange = onUrlChanged,
        maxLines = 12,
    )
}

@Composable
private fun TargetBrowserSelection(
    targetBrowser: TargetBrowser,
    browserPackageNameOptions: List<InstalledBrowser>,
    onTargetBrowserChanged: (TargetBrowser) -> Unit,
) {
    val resources = LocalContext.current.resources
    SelectionField(
        title = stringResource(R.string.label_browser_package_name),
        selectedKey = targetBrowser,
        items = listOf(
            TargetBrowser.Browser(packageName = null) to stringResource(R.string.placeholder_browser_package_name),
            TargetBrowser.CustomTabs(packageName = null) to stringResource(R.string.option_browser_custom_tab),
        ) +
            browserPackageNameOptions.flatMap {
                listOf(
                    TargetBrowser.Browser(it.packageName) to (it.appName ?: it.packageName),
                    TargetBrowser.CustomTabs(it.packageName) to resources.getString(
                        R.string.option_custom_browser_in_custom_tab,
                        (it.appName ?: it.packageName),
                    ),
                )
            },
        onItemSelected = onTargetBrowserChanged,
    )
}

@Composable
private fun MacAddressField(
    savedStateHandle: SavedStateHandle,
    macAddress: String,
    onMacAddressChanged: (String) -> Unit,
) {
    VariablePlaceholderTextField(
        savedStateHandle = savedStateHandle,
        key = "mac-address-input",
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(stringResource(R.string.label_mac_address))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
        ),
        value = macAddress,
        onValueChange = onMacAddressChanged,
        maxLines = 12,
    )
}

@Composable
private fun PortField(
    port: String,
    onPortChanged: (String) -> Unit,
) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(stringResource(R.string.label_wake_on_lan_port))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Number,
        ),
        value = port,
        onValueChange = { text ->
            onPortChanged(text.filter { it.isDigit() }.take(6))
        },
        singleLine = true,
    )
}

@Composable
private fun BroadcastAddressField(
    broadcastAddress: String,
    onBroadcastAddressChanged: (String) -> Unit,
) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(stringResource(R.string.label_broadcast_address))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
        ),
        value = broadcastAddress,
        onValueChange = onBroadcastAddressChanged,
        singleLine = true,
    )
}
