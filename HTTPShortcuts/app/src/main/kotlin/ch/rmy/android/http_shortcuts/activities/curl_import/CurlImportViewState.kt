package ch.rmy.android.http_shortcuts.activities.curl_import

import androidx.compose.runtime.Stable

@Stable
data class CurlImportViewState(
    val submitButtonEnabled: Boolean = false,
    val unsupportedOptions: List<String> = emptyList(),
)
