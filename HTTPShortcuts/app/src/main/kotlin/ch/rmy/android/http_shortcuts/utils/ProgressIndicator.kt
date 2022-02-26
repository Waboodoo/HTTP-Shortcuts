package ch.rmy.android.http_shortcuts.utils

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.framework.utils.Destroyable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity

class ProgressIndicator(private val activity: BaseActivity) : Destroyable {

    private var layoutLoaded = false
    private val showProgressRunnable = Runnable {
        if (!layoutLoaded) {
            layoutLoaded = true
            activity.setContentView(R.layout.activity_execute_loading)
            activity.baseView?.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    fun showProgress() {
        handler.removeCallbacks(showProgressRunnable)
        handler.post(showProgressRunnable)
    }

    fun showProgressDelayed(delay: Long) {
        handler.removeCallbacks(showProgressRunnable)
        handler.postDelayed(showProgressRunnable, delay)
    }

    fun hideProgress() {
        handler.removeCallbacks(showProgressRunnable)
        if (layoutLoaded) {
            activity.baseView?.visible = false
        }
    }

    override fun destroy() {
        handler.removeCallbacks(showProgressRunnable)
    }
}
