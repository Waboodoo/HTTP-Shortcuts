package ch.rmy.android.http_shortcuts.activities.documentation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun ScreenScope.DocumentationScreen(url: Uri?) {
    val (viewModel, state) = bindViewModel<DocumentationViewModel.InitData, DocumentationViewState, DocumentationViewModel>(
        DocumentationViewModel.InitData(url)
    )

    val context = LocalContext.current
    EventHandler { event ->
        when (event) {
            is DocumentationEvent.OpenInBrowser -> consume {
                context.openURL(event.url)
            }
            else -> false
        }
    }

    var subtitle by remember {
        mutableStateOf<String?>(null)
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_documentation),
        subtitle = subtitle,
        actions = {
            ToolbarIcon(
                Icons.Filled.OpenInBrowser,
                contentDescription = stringResource(R.string.button_open_documentation_in_browser),
                onClick = viewModel::onOpenInBrowserButtonClicked,
            )
        },
    ) { viewState ->
        DocumentationContent(
            url = viewState.url,
            onPageChanged = viewModel::onPageChanged,
            onPageTitle = { subtitle = it },
            onExternalUrl = viewModel::onExternalUrl,
        )
    }
}
