package ch.rmy.android.http_shortcuts

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.os.Handler
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.ResponseHandlingModel
import ch.rmy.android.http_shortcuts.scheduling.ExecutionWorker
import javax.inject.Inject


class PollingShortcutsWorker(val context: Context) {

    @Inject
    lateinit var appRepository: AppRepository

    init {
        context.getApplicationComponent().inject(this)
    }

    fun startPolling() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                start()
            }
        })
    }

    @Synchronized
    private fun start() {
        // TODO: replace by Looper
        val handler = Handler()
        // TODO: make delay configurable
        val delay = 20_000L
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isAppOnForeground()) {
                    handler.postDelayed(this, delay)
                }
                // TOOD: This does not work because we don't save this (and don't want to save this),
                // but the ExecutionWork only takes an id.
                // Create a temporary copy from the shortcut?
                appRepository.getPollingShortcuts().blockingGet().forEach {
                    it.executionType = ShortcutExecutionType.TRIGGER.type
                    it.responseHandling = ResponseHandlingModel(
                        ResponseHandlingModel.UI_TYPE_TOAST,
                        ResponseHandlingModel.SUCCESS_OUTPUT_NONE,
                        ResponseHandlingModel.FAILURE_OUTPUT_SIMPLE,
                    )
                    if (javascriptIsClean(it.codeOnPrepare) && javascriptIsClean(it.codeOnFailure) && javascriptIsClean(
                            it.codeOnSuccess
                        )
                    ) {
                        ExecutionWorker.runPollingExecution(context, it)
                    }

                }
            }
        }, delay)
    }

    private fun javascriptIsClean(stringToCheck: String): Boolean {
        // TODO: not very nice solution as the interaction menu might once be extended and this place here might be forgotten.
        val forbiddenUserInteractions =
            arrayOf(
                "alert",
                "showToast",
                "showDialog",
                "showSelection",
                "prompt",
                "confirm",
                "playSound",
                "speak",
                "vibrate",
                "scanBarcode",
            )
        for (f in forbiddenUserInteractions) {
            if (f in stringToCheck.lowercase()) {
                return false
            }
        }
        return true
    }

    private fun isAppOnForeground(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (appProcess in activityManager.runningAppProcesses ?: return false) {
            if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == context.packageName) {
                return true
            }
        }
        return false
    }
}
