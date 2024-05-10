package ch.rmy.android.http_shortcuts.scripting

class CleanupHandler {
    private val cleanupActions = mutableListOf<() -> Unit>()

    fun doFinally(action: () -> Unit) {
        synchronized(cleanupActions) {
            cleanupActions.add(action)
        }
    }

    fun finally() {
        synchronized(cleanupActions) {
            cleanupActions.forEach {
                it.invoke()
            }
            cleanupActions.clear()
        }
    }
}
