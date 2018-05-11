package ch.rmy.android.http_shortcuts.utils

class Destroyer : Destroyable {

    private val destroyables = mutableListOf<Destroyable>()

    fun <T> own(destroyable: T): T where T : Destroyable {
        destroyables.add(destroyable)
        return destroyable
    }

    fun own(destroyable: () -> Unit): Destroyable {
        val destroyableObject = object : Destroyable {
            override fun destroy() {
                destroyable.invoke()
            }
        }
        destroyables.add(destroyableObject)
        return destroyableObject
    }

    override fun destroy() {
        for (destroyable in destroyables) {
            destroyable.destroy()
        }
        destroyables.clear()
    }

}
