package ch.rmy.android.http_shortcuts.activities.editor.headers.models

import androidx.compose.runtime.Stable

@Stable
data class HeaderListItem(
    val id: String,
    val key: String,
    val value: String,
)
