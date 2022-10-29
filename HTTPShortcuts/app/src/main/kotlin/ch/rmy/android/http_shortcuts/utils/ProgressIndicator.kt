package ch.rmy.android.http_shortcuts.utils

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import androidx.core.view.isVisible
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity

class ProgressIndicator(private val activity: BaseActivity) {

    private var layoutLoaded = false
    private val showProgressRunnable = Runnable {
        if (!layoutLoaded) {
            layoutLoaded = true
            activity.setContentView(R.layout.activity_execute_loading)
            activity.baseView?.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    fun showProgressDelayed(delay: Long) {
        handler.removeCallbacks(showProgressRunnable)
        handler.postDelayed(showProgressRunnable, delay)
    }

    fun hideProgress() {
        handler.removeCallbacks(showProgressRunnable)
        if (layoutLoaded) {
            activity.baseView?.isVisible = false
        }
    }

    fun destroy() {
        handler.removeCallbacks(showProgressRunnable)
    }
}
