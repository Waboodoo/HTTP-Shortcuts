package ch.rmy.android.http_shortcuts.utils

class Destroyer : Destroyable {

    private val destroyables = mutableListOf<Destroyable>()

    fun <T> own(destroyable: T): T where T : Destroyable {
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
