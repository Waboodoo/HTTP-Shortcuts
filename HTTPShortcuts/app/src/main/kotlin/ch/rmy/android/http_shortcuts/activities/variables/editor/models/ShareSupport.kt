package ch.rmy.android.http_shortcuts.activities.variables.editor.models

import androidx.compose.runtime.Stable

@Stable
enum class ShareSupport(val text: Boolean = false, val title: Boolean = false) {
    TEXT(text = true),
    TITLE(title = true),
    TITLE_AND_TEXT(text = true, title = true),
}
