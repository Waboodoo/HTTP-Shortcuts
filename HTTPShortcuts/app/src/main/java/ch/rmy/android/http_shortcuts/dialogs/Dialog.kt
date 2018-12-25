package ch.rmy.android.http_shortcuts.dialogs

import org.jdeferred2.Promise

interface Dialog {

    fun shouldShow(): Boolean

    fun show(): Promise<Unit, Unit, Unit>

}