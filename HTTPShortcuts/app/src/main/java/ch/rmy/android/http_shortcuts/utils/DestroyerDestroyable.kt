package ch.rmy.android.http_shortcuts.utils

interface DestroyerDestroyable : Destroyable {

    val destroyer: Destroyer

    override fun destroy() {
        destroyer.destroy()
    }

}
