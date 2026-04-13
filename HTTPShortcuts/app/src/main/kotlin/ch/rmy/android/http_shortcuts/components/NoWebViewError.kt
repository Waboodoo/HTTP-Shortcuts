package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ch.rmy.android.http_shortcuts.R

@Composable
fun NoWebViewError(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.padding(Spacing.SMALL),
            text = stringResource(R.string.error_no_webview),
            textAlign = TextAlign.Center,
        )
    }
}
