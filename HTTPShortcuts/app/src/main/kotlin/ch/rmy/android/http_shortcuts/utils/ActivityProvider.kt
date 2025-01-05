package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import androidx.annotation.UiThread
import androidx.fragment.app.FragmentActivity
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.activities.misc.host.HostActivity
import ch.rmy.android.http_shortcuts.exceptions.NoActivityAvailableException
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityProvider
@Inject
constructor(
    private val context: Context,
) {
    suspend fun <T> withActivity(action: suspend (FragmentActivity) -> T): T {
        val activeActivity = activeActivities.lastOrNull()?.get()
        if (activeActivity != null) {
            return action(activeActivity)
        }

        try {
            val activity = try {
                HostActivity.start(context)
            } catch (e: Throwable) {
                logException(e)
                throw NoActivityAvailableException()
            }
            return action(activity)
        } finally {
            HostActivity.stop()
        }
    }

    companion object {

        private val activeActivities = mutableListOf<WeakReference<FragmentActivity>>()

        @UiThread
        fun registerActivity(activity: FragmentActivity) {
            if (activity is MainActivity) {
                // workaround for an issue with the "Rerun shortcut" feature, where
                // the MainActivity becomes visible shortly after the ExecuteActivity but
                // isn't in the foreground and therefore shouldn't be returned first.
                activeActivities.add(0, WeakReference(activity))
            } else {
                activeActivities.add(WeakReference(activity))
            }
        }

        @UiThread
        fun deregisterActivity(activity: FragmentActivity) {
            activeActivities.removeIf { reference -> reference.get().let { it == null || it == activity } }
        }
    }
}
