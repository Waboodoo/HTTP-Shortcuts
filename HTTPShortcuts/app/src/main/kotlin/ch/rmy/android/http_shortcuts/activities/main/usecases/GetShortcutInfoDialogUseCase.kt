package ch.rmy.android.http_shortcuts.activities.main.usecases

import android.widget.TextView
import androidx.annotation.CheckResult
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import com.afollestad.materialdialogs.callbacks.onShow
import javax.inject.Inject

class GetShortcutInfoDialogUseCase
@Inject
constructor() {

    @CheckResult
    operator fun invoke(shortcutId: ShortcutId, shortcutName: String) =
        createDialogState {
            title(shortcutName)
                .view(R.layout.shortcut_info_dialog)
                .positive(android.R.string.ok)
                .build()
                .onShow { dialog ->
                    dialog.findViewById<TextView>(R.id.shortcut_id).text = shortcutId
                    dialog.findViewById<TextView>(R.id.deep_link_url).text = getDeepLinkUrl(shortcutId)
                }
        }

    companion object {
        internal fun getDeepLinkUrl(shortcutId: ShortcutId) =
            "http-shortcuts://$shortcutId"
    }
}
