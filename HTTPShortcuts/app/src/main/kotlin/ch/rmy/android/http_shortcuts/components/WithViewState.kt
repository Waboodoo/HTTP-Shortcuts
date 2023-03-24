package ch.rmy.android.http_shortcuts.components

import androidx.compose.runtime.Composable

@Composable
fun <T : Any> WithViewState(viewState: T?, content: @Composable (state: T) -> Unit) {
    if (viewState != null) {
        content(viewState)
    } else {
        LoadingIndicatorV()
    }
}
