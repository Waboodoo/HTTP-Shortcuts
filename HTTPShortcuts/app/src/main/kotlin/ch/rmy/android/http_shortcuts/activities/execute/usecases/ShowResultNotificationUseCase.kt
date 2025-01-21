package ch.rmy.android.http_shortcuts.activities.execute.usecases

import android.content.Context
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.getSafeName
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.notifications.Notifier
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import javax.inject.Inject

class ShowResultNotificationUseCase
@Inject
constructor(
    private val context: Context,
    private val notifier: Notifier,
) {
    suspend operator fun invoke(shortcut: Shortcut, response: ShortcutResponse?, output: String?) {
        val title = shortcut.getSafeName(context)
        if (output == null && FileTypeUtil.isImage(response?.contentType)) {
            val uri = response?.getContentUri(context)
            if (uri != null) {
                notifier.showImageNotification(title, uri)
                return
            }
        }
        notifier.showTextNotification(
            title = title,
            message = output ?: response?.getContentAsString(context),
        )
    }
}
