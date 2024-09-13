package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.models.InstalledBrowser
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.data.models.Shortcut

@Composable
fun BasicRequestSettingsContent(
    methodVisible: Boolean,
    method: String,
    url: String,
    targetBrowser: TargetBrowser,
    targetBrowserChoiceVisible: Boolean,
    browserPackageNameOptions: List<InstalledBrowser>,
    onMethodChanged: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onTargetBrowserChanged: (TargetBrowser) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        if (methodVisible) {
            MethodSelection(method, onMethodChanged)
        }

        UrlField(url, onUrlChanged)

        if (targetBrowserChoiceVisible) {
            TargetBrowserSelection(
                targetBrowser = targetBrowser,
                browserPackageNameOptions = browserPackageNameOptions,
                onTargetBrowserChanged = onTargetBrowserChanged,
            )
        }
    }
}

@Composable
private fun MethodSelection(
    method: String,
    onMethodSelected: (String) -> Unit,
) {
    SelectionField(
        title = stringResource(R.string.label_method),
        selectedKey = method,
        items = METHODS.map { it to it },
        onItemSelected = onMethodSelected,
    )
}

private val METHODS = listOf(
    Shortcut.METHOD_GET,
    Shortcut.METHOD_POST,
    Shortcut.METHOD_PUT,
    Shortcut.METHOD_DELETE,
    Shortcut.METHOD_PATCH,
    Shortcut.METHOD_HEAD,
    Shortcut.METHOD_OPTIONS,
    Shortcut.METHOD_TRACE,
)

@Composable
private fun UrlField(url: String, onUrlChanged: (String) -> Unit) {
    VariablePlaceholderTextField(
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
