package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId

@Stable
sealed class DeepLinkDialogState {
    @Stable
    data object Instructions : DeepLinkDialogState()

    @Stable
    data class ShortcutNotFound(
        val shortcutNameOrId: ShortcutNameOrId,
    ) : DeepLinkDialogState()
}
