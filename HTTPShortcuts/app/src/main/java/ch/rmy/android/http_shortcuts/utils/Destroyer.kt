package ch.rmy.android.http_shortcuts.utils

import java.util.*

class Destroyer : Destroyable {

    private val destroyables = HashSet<Destroyable>()

    fun <T : Destroyable> own(destroyable: T): T {
        destroyables.add(destroyable)
        return destroyable
    }

    override fun destroy() {
        for (destroyable in destroyables) {
            destroyable.destroy()
        }
        destroyables.clear()
    }

}
