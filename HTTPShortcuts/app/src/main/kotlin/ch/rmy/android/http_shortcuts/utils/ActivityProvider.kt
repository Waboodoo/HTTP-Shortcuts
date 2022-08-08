package ch.rmy.android.http_shortcuts.utils

import android.app.Activity
import ch.rmy.android.framework.extensions.safeRemoveIf
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityProvider
@Inject
constructor() {

    fun getActivity(): Activity =
        activeActivities.lastOrNull()?.get() ?: error("No active activity found")

    companion object {

        private val activeActivities = mutableListOf<WeakReference<Activity>>()

        fun registerActivity(activity: Activity) {
            activeActivities.add(WeakReference(activity))
        }

        fun deregisterActivity(activity: Activity) {
            activeActivities.safeRemoveIf { reference -> reference.get().let { it == null || it == activity } }
        }
    }
}
