package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.extensions.isHttpShortcut

@Composable
fun BasicRequestSettingsScreen(
    savedStateHandle: SavedStateHandle,
) {
    val (viewModel, state) = bindViewModel<BasicRequestSettingsViewState, BasicRequestSettingsViewModel>()

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = when (state?.shortcutExecutionType?.isHttpShortcut) {
            true -> stringResource(R.string.section_basic_request)
            false -> stringResource(R.string.section_basic_settings)
            else -> ""
        },
    ) { viewState ->
        when (state?.shortcutExecutionType) {
            ShortcutExecutionType.HTTP -> HttpSettingsContent(
                savedStateHandle = savedStateHandle,
                method = viewState.method,
                url = viewState.url,
                onMethodChanged = viewModel::onMethodChanged,
                onUrlChanged = viewModel::onUrlChanged,
            )
            ShortcutExecutionType.BROWSER -> BrowserSettingsContent(
                savedStateHandle = savedStateHandle,
                url = viewState.url,
                targetBrowser = viewState.targetBrowser,
                browserPackageNameOptions = viewState.browserPackageNameOptions,
                onUrlChanged = viewModel::onUrlChanged,
                onTargetBrowserChanged = viewModel::onTargetBrowserChanged,
            )
            ShortcutExecutionType.MQTT -> MqttSettingsContent(
                savedStateHandle = savedStateHandle,
                url = viewState.url,
                onUrlChanged = viewModel::onUrlChanged,
            )
            ShortcutExecutionType.WAKE_ON_LAN -> WakOnLanSettingsContent(
                savedStateHandle = savedStateHandle,
                macAddress = viewState.wolMacAddress,
                port = viewState.wolPort,
                broadcastAddress = viewState.wolBroadcastAddress,
                onMacAddressChanged = viewModel::onWolMacAddressChanged,
                onPortChanged = viewModel::onWolPortChanged,
                onBroadcastAddressChanged = viewModel::onWolBroadcastAddressChanged,
            )
            ShortcutExecutionType.SCRIPTING,
            ShortcutExecutionType.TRIGGER,
            null,
            -> error("This should never happen")
        }
    }
}
