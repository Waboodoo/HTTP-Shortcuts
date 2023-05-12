package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun BasicRequestSettingsScreen() {
    val (viewModel, state) = bindViewModel<BasicRequestSettingsViewState, BasicRequestSettingsViewModel>()

    BackHandler {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.section_basic_request),
    ) { viewState ->
        BasicRequestSettingsContent(
            methodVisible = viewState.methodVisible,
            method = viewState.method,
            url = viewState.url,
            browserPackageName = viewState.browserPackageName,
            browserPackageNameVisible = viewState.browserPackageNameVisible,
            browserPackageNameOptions = viewState.browserPackageNameOptions,
            onMethodChanged = viewModel::onMethodChanged,
            onUrlChanged = viewModel::onUrlChanged,
            onBrowserPackageNameChanged = viewModel::onBrowserPackageNameChanged,
        )
    }
}
