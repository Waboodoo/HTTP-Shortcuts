package ch.rmy.android.http_shortcuts.media_controls

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MediaControlExecutionTrackerTest {

    private val tracker = MediaControlExecutionTracker()

    @Test
    fun `execution state follows active executions`() {
        assertFalse(tracker.isExecuting.value)

        tracker.onExecutionStarted("first")
        assertTrue(tracker.isExecuting.value)

        tracker.onExecutionStarted("second")
        tracker.onExecutionCompleted("first")
        assertTrue(tracker.isExecuting.value)

        tracker.onExecutionCompleted("second")
        assertFalse(tracker.isExecuting.value)
    }

    @Test
    fun `duplicate lifecycle events are idempotent`() {
        tracker.onExecutionStarted("execution")
        tracker.onExecutionStarted("execution")
        tracker.onExecutionCompleted("unknown")
        assertTrue(tracker.isExecuting.value)

        tracker.onExecutionCompleted("execution")
        tracker.onExecutionCompleted("execution")
        assertFalse(tracker.isExecuting.value)
    }
}
