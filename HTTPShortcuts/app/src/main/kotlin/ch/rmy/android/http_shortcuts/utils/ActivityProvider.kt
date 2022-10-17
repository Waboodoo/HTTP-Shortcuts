package ch.rmy.android.http_shortcuts.utils

import androidx.annotation.UiThread
import androidx.fragment.app.FragmentActivity
import ch.rmy.android.framework.extensions.safeRemoveIf
import ch.rmy.android.http_shortcuts.exceptions.NoActivityAvailableException
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityProvider
@Inject
constructor() {

    fun getActivity(): FragmentActivity =
        activeActivities.lastOrNull()?.get() ?: throw NoActivityAvailableException()

    companion object {

        private val activeActivities = mutableListOf<WeakReference<FragmentActivity>>()

        @UiThread
        fun registerActivity(activity: FragmentActivity) {
            activeActivities.add(WeakReference(activity))
        }

        @UiThread
        fun deregisterActivity(activity: FragmentActivity) {
            activeActivities.safeRemoveIf { reference -> reference.get().let { it == null || it == activity } }
        }
    }
}
