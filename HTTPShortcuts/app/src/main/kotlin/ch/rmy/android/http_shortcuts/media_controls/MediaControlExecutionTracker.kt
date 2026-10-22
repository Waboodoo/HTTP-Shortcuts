package ch.rmy.android.http_shortcuts.media_controls

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class MediaControlExecutionTracker
@Inject
constructor() {

    private val activeExecutionIds = mutableSetOf<String>()
    private val mutableIsExecuting = MutableStateFlow(false)

    val isExecuting = mutableIsExecuting.asStateFlow()

    @Synchronized
    fun onExecutionStarted(executionId: String) {
        activeExecutionIds.add(executionId)
        mutableIsExecuting.value = activeExecutionIds.isNotEmpty()
    }

    @Synchronized
    fun onExecutionCompleted(executionId: String) {
        activeExecutionIds.remove(executionId)
        mutableIsExecuting.value = activeExecutionIds.isNotEmpty()
    }

    companion object {
        const val EXTRA_EXECUTION_ID = "ch.rmy.android.http_shortcuts.media_controls.EXECUTION_ID"
    }
}
