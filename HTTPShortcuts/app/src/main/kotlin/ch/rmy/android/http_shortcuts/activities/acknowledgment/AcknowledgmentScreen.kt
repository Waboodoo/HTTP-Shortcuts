package ch.rmy.android.http_shortcuts.activities.acknowledgment

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.SinglePageBrowser

private const val ACKNOWLEDGMENTS_ASSET_URL = "file:///android_asset/acknowledgments.html"

@Composable
fun AcknowledgmentScreen() {
    SimpleScaffold(
        viewState = Unit,
        title = stringResource(R.string.title_licenses),
    ) {
        SinglePageBrowser(ACKNOWLEDGMENTS_ASSET_URL)
    }
}
