package ch.rmy.android.http_shortcuts.activities.acknowledgment

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.navigation.NavigationRequest
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.LocalEventinator
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.SinglePageBrowser

private const val ACKNOWLEDGMENTS_ASSET_URL = "file:///android_asset/acknowledgments.html"

@Composable
fun AcknowledgmentScreen() {
    val eventinator = LocalEventinator.current
    val onNavigationRequest = { request: NavigationRequest ->
        eventinator.onEvent(ViewModelEvent.Navigate(request))
    }
    SimpleScaffold(
        viewState = Unit,
        title = stringResource(R.string.title_licenses),
    ) {
        SinglePageBrowser(ACKNOWLEDGMENTS_ASSET_URL, onNavigationRequest = onNavigationRequest)
    }
}
