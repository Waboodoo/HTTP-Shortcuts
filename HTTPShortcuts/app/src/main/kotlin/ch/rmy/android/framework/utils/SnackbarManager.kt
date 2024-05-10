package ch.rmy.android.framework.utils

object SnackbarManager {

    private val queue = mutableListOf<SnackbarMessage>()

    fun enqueueSnackbar(message: String, long: Boolean) {
        synchronized(queue) {
            queue.add(SnackbarMessage(message, long))
        }
    }

    fun getEnqueuedSnackbars(): List<SnackbarMessage> {
        synchronized(queue) {
            val messages = queue.toList()
            queue.clear()
            return messages
        }
    }

    data class SnackbarMessage(val message: String, val long: Boolean)
}
