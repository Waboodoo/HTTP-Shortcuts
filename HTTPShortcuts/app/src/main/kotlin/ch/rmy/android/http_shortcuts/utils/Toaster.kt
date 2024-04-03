package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import ch.rmy.android.framework.extensions.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Toaster
@Inject
constructor(
    private val context: Context,
    private val activityProvider: ActivityProvider,
    private val permissionManager: PermissionManager,
) {

    suspend fun showToast(message: CharSequence, long: Boolean, isForeground: Boolean = false) {
        withContext(Dispatchers.Main) {
            if (permissionManager.hasNotificationPermission() || isForeground) {
                context.showToast(message, long)
            } else {
                activityProvider.withActivity {
                    context.showToast(message, long)
                }
            }
        }
    }
}
