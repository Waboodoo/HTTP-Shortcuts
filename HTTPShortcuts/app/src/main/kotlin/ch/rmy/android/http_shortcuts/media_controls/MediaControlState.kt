package ch.rmy.android.http_shortcuts.media_controls

import android.view.KeyEvent

internal sealed interface MediaControlCommand {
    data class MediaButton(val event: KeyEvent) : MediaControlCommand

    data object Previous : MediaControlCommand

    data object Trigger : MediaControlCommand

    data object Next : MediaControlCommand
}

internal class MediaControlState(
    private val maxPendingCommands: Int,
) {
    private val queue = MediaShortcutQueue()
    private val pendingCommands = ArrayDeque<MediaControlCommand>()
    private var initialized = false

    var isExecuting = false
        private set

    val current: MediaControlItem?
        get() = queue.current

    val currentCategory: MediaShortcutCategory?
        get() = queue.currentCategory

    val itemCount: Int
        get() = queue.itemCount

    val selectedItemNumber: Int
        get() = queue.selectedItemNumber

    fun replaceCatalog(catalog: MediaControlCatalog): List<MediaControlCommand> {
        queue.replace(catalog)
        initialized = true
        return buildList {
            while (pendingCommands.isNotEmpty()) {
                add(pendingCommands.removeFirst())
            }
        }
    }

    fun accept(command: MediaControlCommand): List<MediaControlCommand> {
        if (initialized) {
            return listOf(command)
        }
        if (pendingCommands.size < maxPendingCommands) {
            pendingCommands.addLast(command)
        }
        return emptyList()
    }

    fun next() {
        queue.next()
    }

    fun previous() {
        queue.previous()
    }

    fun changeCategory(): Boolean = queue.changeCategory()

    fun setExecuting(isExecuting: Boolean) {
        this.isExecuting = isExecuting
    }
}
