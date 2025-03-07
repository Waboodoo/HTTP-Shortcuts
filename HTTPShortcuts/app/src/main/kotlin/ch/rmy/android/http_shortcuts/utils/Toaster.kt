package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import ch.rmy.android.framework.extensions.showToast
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Toaster
@Inject
constructor(
    private val context: Context,
    private val activityProvider: ActivityProvider,
    private val permissionManager: PermissionManager,
) {

    suspend fun showToast(message: CharSequence, long: Boolean) {
        withContext(Dispatchers.Main) {
            if (permissionManager.hasNotificationPermission()) {
                context.showToast(message, long)
            } else {
                activityProvider.withActivity {
                    context.showToast(message, long)
                }
            }
        }
    }
}
