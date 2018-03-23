package ch.rmy.android.http_shortcuts.utils

interface Destroyable {

    fun destroy()

    fun attachTo(destroyer: Destroyer) = destroyer.own(this)

}
