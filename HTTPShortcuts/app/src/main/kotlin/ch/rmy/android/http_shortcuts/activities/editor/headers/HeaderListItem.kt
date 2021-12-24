package ch.rmy.android.http_shortcuts.activities.editor.headers

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R

sealed interface HeaderListItem {
    data class Header(
        val id: String,
        val key: String,
        val value: String,
    ) : HeaderListItem

    object EmptyState : HeaderListItem {
        val title: Localizable
            get() = StringResLocalizable(R.string.empty_state_request_headers)

        val instructions: Localizable
            get() = StringResLocalizable(R.string.empty_state_request_headers_instructions)
    }
}
