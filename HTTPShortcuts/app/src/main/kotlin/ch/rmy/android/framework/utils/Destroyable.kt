package ch.rmy.android.framework.utils

interface Destroyable {

    fun destroy()

    fun attachTo(destroyer: Destroyer) = destroyer.own(this)
}
