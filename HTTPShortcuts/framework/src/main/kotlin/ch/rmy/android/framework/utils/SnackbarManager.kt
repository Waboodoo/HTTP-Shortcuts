package ch.rmy.android.framework.utils

import android.app.Activity
import android.view.ViewGroup
import androidx.annotation.UiThread
import com.google.android.material.snackbar.Snackbar

object SnackbarManager {

    private val queue = mutableListOf<SnackbarMessage>()

    @UiThread
    fun showSnackbar(activity: Activity, message: CharSequence, long: Boolean) {
        if (activity.isFinishing) {
            queue.add(SnackbarMessage(message, long))
            return
        }
        showSnackbarInternal(activity, message, long)
    }

    private fun showSnackbarInternal(activity: Activity, message: CharSequence, long: Boolean) {
        val baseView = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        Snackbar.make(baseView, message, if (long) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT).show()
    }

    @UiThread
    fun showEnqueuedSnackbars(activity: Activity) {
        if (activity.isFinishing) {
            return
        }
        queue.forEach { snackbarMessage ->
            showSnackbarInternal(activity, snackbarMessage.message, snackbarMessage.long)
        }
        queue.clear()
    }

    private data class SnackbarMessage(val message: CharSequence, val long: Boolean)
}
