package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.notifications.Notifier
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import javax.inject.Inject

class NotificationAction
@Inject
constructor(
    private val notifier: Notifier,
    private val permissionManager: PermissionManager,
) : Action<NotificationAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        if (permissionManager.requestNotificationPermissionIfNeeded() && title.isNotEmpty()) {
            notifier.showTextNotification(
                title = title,
                message = message,
            )
        }
    }

    data class Params(
        val title: String,
        val message: String?,
    )
}
