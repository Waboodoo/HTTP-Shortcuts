package ch.rmy.android.http_shortcuts.activities.main.usecases

import android.widget.TextView
import androidx.annotation.CheckResult
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import com.afollestad.materialdialogs.callbacks.onShow

class GetShortcutInfoDialogUseCase {

    @CheckResult
    operator fun invoke(shortcutId: String, shortcutName: String) =
        DialogState.create {
            title(shortcutName)
                .view(R.layout.shortcut_info_dialog)
                .positive(android.R.string.ok)
                .build()
                .apply {
                    onShow {
                        it.findViewById<TextView>(R.id.shortcut_id).text = shortcutId
                        it.findViewById<TextView>(R.id.deep_link_url).text = getDeepLinkUrl(shortcutId)
                    }
                }
        }

    companion object {
        private fun getDeepLinkUrl(shortcutId: String) =
            "http-shortcuts://$shortcutId"
    }
}
